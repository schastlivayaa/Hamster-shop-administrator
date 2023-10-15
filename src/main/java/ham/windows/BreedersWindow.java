package ham.windows;

import ham.entity.*;
import ham.save.savePDF;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import static javax.swing.GroupLayout.Alignment.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

import org.apache.log4j.Logger;

/**
 * Класс экранной формы для отображения информации о разводчиках
 * @author Злата Счастливая
 */
public class BreedersWindow {
	/** Окно со списком разводчиков */
	private JFrame window;
	
	/** Кнопка "Назад" */
	private JButton backButton;
	
	/** Панель с кнопкой "Назад" */
	private JPanel Panel;
	
	/** Панель инструментов */
	private JPanel toolPanel;
	
	/** Кнопка "Сохранить список" 
	private JButton saveButton;*/
	
	/** Кнопка "Добавить строку" */
	private JButton addButton;
	
	/** Кнопка "Создать отчет" */
	private JButton printButton;
	
	/** Заголовок таблицы */
	private String[] header;
	
	/** Список разводчиков */
	private List<Breeder> allBreeders = new ArrayList<Breeder>();
	
	/** Список пород */
	List<Hamster> allHamsters = new ArrayList<Hamster>();
	
	/** Модель таблицы */
	private DefaultTableModel model;
	
	/** Таблица с информацией о разводчиках */
	private JTable table;
	
	/** Прокрутка */
	private JScrollPane scroll;
	
	/** Строка поиска породы */
	private JTextField speciesFilter;
	
	/** Поиск */
	private JButton searchButton;
	
	/** Контекстное меню */
	private JPopupMenu popupmenu;
	
	/** Элемент контекстного меню */
	private JMenuItem editButton;
	
	/** Элемент контекстного меню */
    private JMenuItem deleteButton;
	
	/** Координаты выделенной строки */
	private Point point;
	
	/** Выбранная строка */
	int selectRow;
	
	/** Выбранный столбец */
	int selectColumn;
	
	/** Объект для записи сообщений протоколирования */
	private static final Logger log = Logger.getLogger("BreedersWindow.class");
	
	/** Конструктор класса */
	public BreedersWindow() {
		log.info("Создание экранной формы");
		window = new JFrame("Сведения о разводчиках");
		window.setSize(700, 600);
		window.setLocation(400, 100);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container container = window.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		// Создание кнопки возврата в главное меню
		backButton = new JButton("Назад");
		Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Panel.add(backButton); // Размещение кнопки на панели		
		container.add(Panel); // Размещение панели в контейнере
		
		// Создание кнопок панели инструментов
		//saveButton = new JButton("Сохранить");
		addButton = new JButton("Добавить");
		printButton = new JButton("Отчет");
		//saveButton.setToolTipText("Сохранить список в файл"); // Настройка подсказок для кнопок
		addButton.setToolTipText("Добавить строку в таблицу");
		printButton.setToolTipText("Сделать отчет");
		
		// Создание панели инструментов
		toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		toolPanel.setBorder(BorderFactory.createTitledBorder("Панель инструментов"));
		//toolPanel.add(saveButton); // Добавление кнопок на панель инструментов
		toolPanel.add(addButton);
		toolPanel.add(printButton);
		container.add(toolPanel);  // Размещение панели в контейнере
		
		// Создание таблицы с данными
		header = new String[]{"Фамилия Имя", "Породы хомяков"};
		model = new DefaultTableModel(null, header) {
			@Override
			public boolean isCellEditable(int i, int i1) {
				if (i == selectRow && i1 == selectColumn) return true;
				else return false; // Запрет на редактирование ячеек таблицы
			}
		};
		getBreedersFromDB(); // запись данных из БД
		table = new JTable(model);
		table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14)); // шрифт заголовка
		table.setFont(new Font("Arial", Font.PLAIN, 12)); // шрифт таблицы
		table.setRowHeight(30); // высота ячеек
		scroll = new JScrollPane(table); // добавление прокрутки
		container.add(scroll); // Размещение таблицы с данными
		
		// Подготовка компонентов поиска
		speciesFilter = new JTextField("Название породы");
		searchButton = new JButton("Поиск");
		searchButton.setBackground(Color.GRAY);
		
		// Создание панели поиска
		JPanel filterPanel = new JPanel();
		filterPanel.add(speciesFilter);
		filterPanel.add(searchButton);
		filterPanel.setBackground(Color.LIGHT_GRAY);
		container.add(filterPanel); // Размещение панели в контейнере
		
		// создание контекстного меню
		popupmenu = new JPopupMenu();
		// создание и добавление элементов меню
		editButton = new JMenuItem("Редактировать ячейку");
		editButton.setBorder(BorderFactory.createLineBorder(Color.gray));  
        deleteButton = new JMenuItem("Удалить строку");
        deleteButton.setBorder(BorderFactory.createLineBorder(Color.gray));
		popupmenu.add(editButton);
		popupmenu.add(deleteButton);
		// Размещение контекстного меню
		table.add(popupmenu);
		
		// добавление слушателей
		addListeners();
		
		// отображение экранной формы
		show();
	}
	
	/**
	 * Демонстрация экранной формы
	 */
	public void show() {
		window.setVisible(true);
	}
	
	/**
	 * Метод добавления слушателей к элементам
	 */
	private void addListeners() {
		/**
		 * Блок прослушивания кнопки "Назад"
		 * При нажатии на кнопку "Назад" текущее окно закрывается
		 */
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.info("Нажатие кнопки 'Назад'");
				window.dispose();
			}
		});
		
		/**
		 * Блок прослушивания кнопки "Сохранить список"
		 * При нажатии на кнопку появляется диалоговое окно 
		 * с предоставлением выбора файла для сохранения
		 
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.info("Сохранение данных в файл");
				//new FileSave("Сохранение данных", model);
			}
		});*/
		
		/**
		 * Блок прослушивания кнопки "Добавить строку"
		 * При нажатии на кнопку появляется диалоговое окно для введения данных
		 */
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.info("Добавление строки в таблицу");
				addNewElem();
			}
		});
		
		/**
		 * Блок прослушивания кнопки "Создать отчет"
		 * При нажатии на кнопку появляется диалоговое окно 
		 * с предоставлением выбора файла для сохранения
		 */
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.info("Нажатие кнопки 'Создать отчет'");
				try {
					log.warn("Создание отчета. Может возникнуть исключение");
					checkTable(model);
					new savePDF(model, header, "Сведения о разводчиках");
				}
				catch(EmptyTable e) {
					log.error("Пустая таблица!", e);
					JOptionPane.showMessageDialog(window, e.getMessage());
				}
			}
		});
		
		/**
		 * Блок прослушивания строки поиска (нажатие мышью)
		 * При выполнении одного щелчка на строке поиска текст выделяется
		 */
		speciesFilter.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent event)
			{
				speciesFilter.selectAll();
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		
		/**
		 * Блок прослушивания кнопки "Поиск"
		 * При нажатии на кнопку происходит поиск по JTextBox
		 */
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					log.warn("Поиск элемента в таблице. Может возникнуть ошибка введенного текста");
					checkSearch(speciesFilter); // проверка строки поиска
					TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(((DefaultTableModel) model));
					sorter.setStringConverter(new TableStringConverter() {
				        @Override
				        public String toString(TableModel model, int row, int column) {
				            return model.getValueAt(row, column).toString().toLowerCase();
				        }
				    });
					log.info("Поиск элементов по запросу" + speciesFilter.getText());
					sorter.setRowFilter(RowFilter.regexFilter(speciesFilter.getText().toLowerCase(), 1));
					table.setRowSorter(sorter);
				}
				catch(MyExceptionSearch myEx) { 
					log.error("Поиск по строке 'Название породы'", myEx);
					JOptionPane.showMessageDialog(window, myEx.getMessage());
				}
				catch(NotString ex) {
					log.error("В строке содержатся цифры", ex);
					JOptionPane.showMessageDialog(window, ex.getMessage());
				}
			}
		});
		
		
		/**
		 * Блок прослушивания ПКМ
		 * Вызов контекстного меню при нажатии ПКМ на ячейке таблицы
		 */
		table.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent event) {}
			public void mouseClicked(MouseEvent event) {
				//если нажатие левой кнопкой мыши
				if(event.getButton() == MouseEvent.BUTTON1) {
					selectColumn = -1;
					selectRow = -1;
				}
				
				// если нажатие правой кнопкой мыши
				if(event.getButton() == MouseEvent.BUTTON3) {
					log.info("Вызов контекстного меню");
					// вычисление координаты точки нажатия
					point = event.getPoint();
					
					// получаем номера строки и столбца, где произошло нажатие
					selectColumn = table.columnAtPoint(point);
					selectRow = table.rowAtPoint(point);
					
					// выделение строки
					table.setColumnSelectionInterval(selectColumn, selectColumn);
					table.setRowSelectionInterval(selectRow, selectRow);
					
					// отображение контекстного меню
					popupmenu.show(table, event.getX(), event.getY());
				}
			}
			public void mouseReleased(MouseEvent event) {}
			public void mouseEntered(MouseEvent event) {}
			public void mouseExited(MouseEvent event) {}
		});
		
		/**
		 * Блок прослушивания кнопки "Удалить"
		 * При нажатии происходит удаление выбранной строки
		 */
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				log.info("Удаление строки");
				int choice = JOptionPane.showConfirmDialog(window, 
						"Вы действительно хотите удалить данные?\nЭто действие невозможно отменить", 
						"Уведомление", JOptionPane.YES_NO_OPTION);
				if (choice == 0) {
					dellRowInDB(selectRow);
					model.removeRow(selectRow);
					JOptionPane.showMessageDialog(window, "Данные успешно удалены");
					log.debug("Удаление строки");
				}
				else log.debug("Пользователь нажал 'Отмена'");
			}
		});
		
		/**
		 * Блок прослушивания кнопки "Редактировать"
		 * При нажатии предоставляется возможность изменить содержимое выбранной ячейки
		 */
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				log.info("Редактирование ячейки таблицы");
				editElem(selectRow, selectColumn);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private List<Breeder> getAllBreeders() {
		// Создание фабрики EntityManager
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		// Создаем очередь разводчиков из таблицы breeder
		Query bq = em.createQuery("SELECT e FROM Breeder e");
		// Преобразуем очередь в список элементов класса Breeder
		List<Breeder> allBreeds = bq.getResultList();
		em.getTransaction().commit();
		em.close();
		
		return allBreeds;
	}
	
	/** Метод загрузки данных из БД */
	private void getBreedersFromDB() {
		log.info("Загрузка списка разводчиков");
		// Создание фабрики EntityManager
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		allBreeders = getAllBreeders();
		// Массив элементов таблицы
		String[] data = new String[2];
		if (allBreeders != null) {
			for(Breeder breeder : allBreeders) {
				try {
					data[0] = breeder.getName();
					data[1] = breeder.getHamsStr();
					model.addRow(data); // добавление строки в таблицу
				} catch (StringIndexOutOfBoundsException ex) {
					Query searchBreed = em.createQuery("SELECT e FROM Breeder e WHERE e.name =:name");
					searchBreed.setParameter("name", breeder.getName());
					em.remove(searchBreed.getSingleResult());
				}
			}
		}
		em.getTransaction().commit();
		em.close();
	}

	/**
	 * Метод удаления строки в базе данных
	 * @param row
	 */
	private void dellRowInDB(int row) {
		allBreeders = getAllBreeders();
		String breeder = (String) model.getValueAt(row, 0); 
		for(Breeder newBred : allBreeders) { // по всем существующим разводчикам
			if(newBred.getName().equals(breeder)) { // нашли разводчика
				EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
			    EntityManager em = emf.createEntityManager();
			    em.getTransaction().begin();
			    newBred.setHamListToNull();
			    Query bq = em.createQuery("DELETE FROM Breeder e WHERE e.name = :name");
			    bq.setParameter("name", breeder);
			    bq.executeUpdate();
				em.getTransaction().commit();
			    em.close();
			    break;
			}
		}
	}
	/**
	 * Метод редактирования ячейки таблицы
	 * @param row - выбранная строка
	 * @param col - выбранный столбец
	 */
	private void editElem(int row, int col) {
		// создание панели с полем для редактирования
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JTextField newText = new JTextField(20);
		JLabel label;
		
		if (col == 0) label = new JLabel("Разводчик: "); // если выделен первый столбец таблицы
		else label = new JLabel("Породы: "); // если выделен второй столбец таблицы
		panel.add(label);
		panel.add(newText);
		
		// демострация диалогового окна и получение ответа пользователя
		int choice = JOptionPane.showOptionDialog(window, panel, "Редактировать информацию", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		
		if (choice == 0) {
			String pevBreed = (String) model.getValueAt(row, 0); // разводчик, о котором меняется информ.
			String editedText = newText.getText(); // получаем текст из текстового поля
			try {
				log.warn("Проверка введенного пользователем текста. Может возникнуть ошибка");
				if(col == 0) {
					checkNewStr(editedText, null); // проверяем редактирование ячейки разводчика
					editElemDB(editedText, pevBreed, 0);
				}
				else {
					checkNewStr(null, editedText); // проверяем редактирование ячейки хомяка
					editElemDB(editedText, pevBreed, 1);
				}
				model.setValueAt(editedText, row, col);; // изменение текста в ячейке
			}
			catch(ExistElement ex) { 
				log.error("Добавление уже существующего элемента", ex);
				JOptionPane.showMessageDialog(null, "Элемент " + editedText + " уже существует!",
						"Ошибка", JOptionPane.INFORMATION_MESSAGE,
						null);
			}
			catch(NotString ex) { 
				log.error("Добавление строки, содержащей числа", ex);
				JOptionPane.showMessageDialog(null, "Не используйте цифры!",
						"Ошибка", JOptionPane.INFORMATION_MESSAGE,
						null);
			}
			catch(NullPointerException ex) {
				JOptionPane.showMessageDialog(window, ex.getMessage());
				log.error("Пустая строка", ex);
			}
			catch(NonExistHamster ex) {
				JOptionPane.showMessageDialog(window, ex.getMessage());
				log.error("Породы не существует", ex);
			}
		}
		else log.debug("Пользователь нажал 'Отмена'");
	}
	
	private void editElemDB(String newText, String prevBreed, int colNum) {
		allBreeders = getAllBreeders();
		if(colNum == 0) {
			allBreeders = getAllBreeders();
			for(Breeder newBred : allBreeders) { // по всем существующим разводчикам
				System.out.println(1);
				if(newBred.getName().equals(prevBreed)) { // нашли разводчика
					System.out.println(2);
					EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
				    EntityManager em = emf.createEntityManager();
				    em.getTransaction().begin();
				    newBred.setName(newText);
					em.merge(newBred);
					em.getTransaction().commit();
				    em.close();
				    break;
				}
			}
		}
		if(colNum == 1) {
			allHamsters = HamstersWindow.getAllHamsters(); // список всех пород
			for(Breeder newBred : allBreeders) { // по всем существующим разводчикам
				if(newBred.getName().equals(prevBreed)) { // нашли разводчика
					EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
				    EntityManager em = emf.createEntityManager();
				    em.getTransaction().begin();
					newBred.setHamListToNull();// удалить связи разводчик-хомяки
					
					String[] newHamsters = newText.split("\\s*;\\s*"); // массив пород
					for(int i = 0; i < newHamsters.length; i++) {
						String hamster = newHamsters[i]; // одна порода из массива
						for(Hamster newHam : allHamsters) { // по всем существующим породам
							if(newHam.getName().equals(hamster)) { // если совпадает
								newBred.addHam(newHam); // добавление новой связи
								em.merge(newBred);
							    em.merge(newHam);
							}
						}
					}
					em.getTransaction().commit();
				    em.close();
				}
			}
		}
	}
	
	/** Метод создания и добавления нового элемента в список */
	private void addNewElem() {
		JPanel panel = new JPanel();
		
		// Определение менеджера расположения
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true); 
        layout.setAutoCreateContainerGaps(true);
        
        // Компоненты формы
        JLabel label1 = new JLabel("Разводчик:");
        JTextField textBreeder = new JTextField(20);
        JLabel label2 = new JLabel("Породы:");
        JTextField textSpecieses = new JTextField(20);
        JLabel example = new JLabel("Вводите породы через ';'");
        example.setFont(new Font("Verdana", Font.PLAIN, 11));
        
        // Создание горизонтальной группы
        layout.setHorizontalGroup(layout.createSequentialGroup() 
                .addGroup(layout.createParallelGroup(LEADING) 
                		.addComponent(label1)
                		.addComponent(label2))
                .addGroup(layout.createParallelGroup(LEADING)
                        .addComponent(textBreeder) 
                        .addComponent(textSpecieses)
                        .addComponent(example))                		
        );
        layout.linkSize(SwingConstants.HORIZONTAL, textBreeder, textSpecieses);  // связываем размеры объектов
        // Создание вертикальной группы
        layout.setVerticalGroup(layout.createSequentialGroup() 
                .addGroup(layout.createParallelGroup(BASELINE) 
                        .addComponent(label1) 
                        .addComponent(textBreeder))
                .addGroup(layout.createParallelGroup(BASELINE) 
                        .addComponent(label2) 
                        .addComponent(textSpecieses))
                .addGroup(layout.createParallelGroup(BASELINE) 
                        .addComponent(example))
        );
		
        // демострация диалогового окна и получение ответа пользователя
		int choice = JOptionPane.showOptionDialog(window, panel, "Добавить информацию", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		
		if (choice == 0) {
			String newBreeder = textBreeder.getText(); // получаем текст
			String newSpecies = textSpecieses.getText();
			try {
				log.warn("Проверка введенного пользователем текста. Может возникнуть ошибка");
				checkNewStr(newBreeder, newSpecies); // проверяем введенный текст
				
				log.info("Добавление новой строки");
				addElemToDB(newBreeder, newSpecies);
				//getBreedersFromDB();
				//model.addRow(new Object[] {newBreeder, newSpecies}); // добавление строки в таблицу
			}
			catch(ExistElement ex) { 
				log.error("Добавление уже существующего разводчика", ex);
				JOptionPane.showMessageDialog(window, "Элемент " + newBreeder + " уже существует!",
						"Ошибка", JOptionPane.INFORMATION_MESSAGE,
						null);
			}
			catch(NotString ex) {
				log.error("Добавление строки, содержащей числа", ex);
				JOptionPane.showMessageDialog(window, ex.getMessage(),
						"Ошибка", JOptionPane.INFORMATION_MESSAGE,
						null);
			}
			catch(NullPointerException ex) {
				JOptionPane.showMessageDialog(window, ex.getMessage());
				log.error("Пустая строка", ex);
			}
			catch(NonExistHamster ex) {
				JOptionPane.showMessageDialog(window, ex.getMessage());
				log.error("Породы не существует", ex);
			}
		}
		else log.debug("Пользователь нажал 'Отмена'");
	}
	
	private void addElemToDB(String breeder, String hamStr) {
		allHamsters = HamstersWindow.getAllHamsters(); // список всех пород
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
	    EntityManager em = emf.createEntityManager();
	    
	    em.getTransaction().begin();
		Breeder newBreeder = new Breeder(breeder);
		em.persist(newBreeder);
		
		String[] newHamsters = hamStr.split("\\s*;\\s*"); // массив пород
		for(int i = 0; i < newHamsters.length; i++) {
			String hamster = newHamsters[i]; // одна порода из массива
			for(Hamster newHam : allHamsters) { // по всем существующим породам
				if(newHam.getName().equals(hamster)) { // если совпадает
					newBreeder.addHam(newHam);
					em.merge(newBreeder);
				    em.merge(newHam);
				}
			}
		}
		model.addRow(new Object[] {newBreeder.getName(), hamStr}); // добавление строки в таблицу
		em.getTransaction().commit();
	    em.close();
	}
	
	/**
	 * Метод контроля добавления элементов в список
	 * @param breeder - ФИ довабляемого разводчика
	 * @throws ExistElement - исключение повторного добавления элемента
	 * @throws NonExistHamster 
	 */
	private void checkNewStr(String breeder, String hamsters) throws ExistElement, NotString, NullPointerException, NonExistHamster{
		// проверка на повтор имени разводчика
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0).equals(breeder)) throw new ExistElement(); // если совпадает с 
																//существующим элементом списка, ошибка
		}
		
		if(breeder != null) {
			if(breeder.length() == 0) throw new NullPointerException(); // пустая строка
			
			for(int i = 0; i < breeder.length(); i++) // проверка строки разводчика на наличие чисел
	            if(Character.isDigit(breeder.charAt(i))) throw new NotString();
		}
        if(hamsters != null) {
        	if(hamsters.length() == 0) throw new NullPointerException(); // пустая строка
        	
        	for(int i = 0; i < hamsters.length(); i++) // проверка строки хомяков на наличие чисел
                if(Character.isDigit(hamsters.charAt(i))) throw new NotString();
        	
        	// проверка на существование породы
    		List<Hamster> allHams = HamstersWindow.getAllHamsters(); // существующие породы
        	boolean matched = false; // совпадение
    		String[] newHamsters = hamsters.split("\\s*;\\s*"); // массив пород
    		for(int i = 0; i < newHamsters.length; i++) { // по всем породам
    			String hamster = newHamsters[i]; // одна порода из массива
    			hamster = hamster.toLowerCase();
    			for(Hamster exHam : allHams) { // по всем существующим породам
    				if(exHam.getName().toLowerCase().equals(hamster)) matched = true;
    			}
    			if(matched == false) throw new NonExistHamster(hamster);
    		}
        }
	}
	
	/**
	 * Метод контроля значения ввода названия породы при поиске
	 * @param species - текстовое поле для ввода
	 * @throws MyExceptionSearch - обработка строки со значением "Название породы"
	 * @throws NullPointerException - исключение пустой строки
	 */
	private void checkSearch(JTextField speciesFilter) throws MyExceptionSearch, NotString {
		String sName = speciesFilter.getText();
		if(sName.contains("Название породы")) throw new MyExceptionSearch();
		
		for(int i = 0; i < sName.length(); i++) { // проверка строки хомяков на наличие чисел
            if(Character.isDigit(sName.charAt(i))) throw new NotString();
        }
	}
	
	/**
	 * Метод проверки таблицы на наличие строк
	 * @param model - модель таблицы
	 * @throws EmptyTable - исключение, пустая таблица
	 */
	private void checkTable(DefaultTableModel model) throws EmptyTable {
		if(model.getRowCount() == 0) throw new EmptyTable(); 
	}
	
	/** Собственное исключение: ошибка при добавлении элемента */
	private class ExistElement extends Exception{
		/** Ошибка: добавление уже существующего в списке разводчика */
		public ExistElement() {
			super("Этот разводчик уже есть в списке!");
		}
	}
	/** Собственное исключение: ошибка при добавлении элемента */
	private class NotString extends Exception{
		/** Ошибка: добавление чисел */
		public NotString() {
			super("Не используйте цифры!");
		}
	}
	/** Собственное исключение: ошибка при поиске породы */
	private class MyExceptionSearch extends Exception{
		/** Ошибка: поле ввода содержит строку "Название породы" */
		public MyExceptionSearch() {
			super("Вы не ввели название породы для поиска");
		}
	}
	/** Собственное исключение: ошибка при поиске */
	private class NullPointerException extends Exception{
		/** Ошибка: поле ввода пустое" */
		public NullPointerException() {
			super("Все поля должны быть заполнены!");
		}
	}
	/** Собственное исключение: ошибка при сохранении */
	private class EmptyTable extends Exception{
		/** Ошибка: пустая таблица */
		public EmptyTable() {
			super("Таблица пуста!");
		}
	}

	private class NonExistHamster extends Exception{
		public NonExistHamster(String hamster) {
			super("Породы " + hamster + " не существует!");
		}
	}
	
}
