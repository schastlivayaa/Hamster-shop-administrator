package ham.windows;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import ham.entity.Breeder;
import ham.entity.Hamster;
import ham.save.savePDF;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

/**
 * Класс экранной формы для отображения информации о породах
 * @author Злата Счастливая
 *
 */
public class HamstersWindow {
	/** Окно со списком пород */
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
	
	/** Модель таблицы */
	private DefaultTableModel model;
	
	/** Таблица со списком пород */
	private JTable table;
	
	/** Прокрутка */
	private JScrollPane scroll;
	
	/** Строка поиска пород */
	private JTextField speciesFilter;
	
	/** Поиск */
	private JButton searchButton;
	
	/** Список всех пород хомяков */
	private List<Hamster> allHamsters = new ArrayList<Hamster>();
	
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
	private static final Logger log = Logger.getLogger("HamstersWindow.class");
	
	/** Конструктор класса */
	public HamstersWindow() {
		log.info("Создание экранной формы");
		window = new JFrame("Сведения о хомяках");
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
		header = new String[]{"Порода", "Цена", "Уход"};
		model = new DefaultTableModel(null, header) {
			@Override
			public boolean isCellEditable(int i, int i1) { 
				if (i == selectRow && i1 == selectColumn) return true;
				else return false; // Запрет на редактирование ячеек таблицы
			}
		};
		getHamstersFromDB(); // запись данных из БД
		table = new JTable(model);
		table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14)); // шрифт заголовка
		table.setFont(new Font("Arial", Font.PLAIN, 12)); // шрифт таблицы
		table.setRowHeight(30); // высота ячеек
		table.getColumnModel().getColumn(0).setMinWidth(100); // ширина столбцов
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.getColumnModel().getColumn(1).setMaxWidth(100);
		scroll = new JScrollPane(table); // добавление прокрутки
		container.add(scroll); // Размещение таблицы с данными
		
		// Подготовка компонентов поиска
		speciesFilter = new JTextField("Название породы");
		searchButton = new JButton("Поиск");
		searchButton.setBackground(Color.GRAY);
		
		// Добавление компонентов на панель поиска
		JPanel filterPanel = new JPanel();
		filterPanel.setBackground(Color.LIGHT_GRAY);
		filterPanel.add(speciesFilter);
		filterPanel.add(searchButton);
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
	 * Метод получения из базы данных списка всех пород
	 * @return allHams - список всех пород
	 */
	@SuppressWarnings("unchecked")
	public static List<Hamster> getAllHamsters(){
		// Создание фабрики EntityManager
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		// Создаем очередь пород из таблицы hamster
		Query qh = em.createQuery("SELECT e FROM Hamster e");
		// Преобразуем очередь в список элементов класса Hamster
		List<Hamster> allHams = qh.getResultList();
		em.getTransaction().commit();
		em.close();
		
		return allHams;
	}	
	
	/**
	 * Метод записи информации о хомяках в таблицу
	 */
	private void getHamstersFromDB() {
		log.info("Загрузка списка хомяков");
		// Создание фабрики EntityManager
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		allHamsters = getAllHamsters();
		// Массив элементов таблицы
		String[] data = new String[3];
		if (allHamsters != null) {
			for(Hamster hamster : allHamsters) {
				data[0] = hamster.getName();
				data[1] = Integer.toString(hamster.getPrice());
				data[2] = hamster.getRules();
				model.addRow(data); // добавление строки в таблицу
			}
		}
		em.getTransaction().commit();
		em.close();
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
					new savePDF(model, header, "Сведения о породах");
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
					hamsterSearch(speciesFilter); // поиск
				}
				catch(NullPointerException ex) {
					log.error("Строка пуста!", ex);
					JOptionPane.showMessageDialog(window, ex.toString());
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
					
					popupmenu.show(table, event.getX(), event.getY());	// отображение контекстного меню
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
					selectRow = table.getSelectedRow();
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
	
	
	/**
	 * Метод удаления строки из базы данных
	 * @param row - номер строки
	 */
	@SuppressWarnings("unchecked")
	private void dellRowInDB(int row) {
		allHamsters = getAllHamsters();
		String hamster = (String) model.getValueAt(row, 0); 
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Query searchHam = em.createQuery("SELECT e FROM Hamster e "
				+ "WHERE e.name = :name");
		searchHam.setParameter("name", hamster);
		Hamster gHam = (Hamster) searchHam.getSingleResult();
		
		List<Breeder> allBreeds = em.createQuery("SELECT e FROM Breeder e").getResultList();
		for (Breeder breed : allBreeds) {
			Set<Hamster> hamsBreed = breed.getHams();
			if(breed.checkHamster(gHam.getName())) {
				hamsBreed.remove(gHam);
			}
			
		}
		
		em.remove(gHam);
		em.getTransaction().commit();
		em.close();
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
		JLabel label = null;
		
		if (col == 0) label = new JLabel("Название породы: "); // если выделен первый столбец таблицы
		else if(col == 1) label = new JLabel("Цена: "); // если выделен второй столбец таблицы
		else label = new JLabel("Правила ухода: ");
		panel.add(label);
		panel.add(newText);
		
		// демострация диалогового окна и получение ответа пользователя
		int choice = JOptionPane.showOptionDialog(window, panel, "Редактировать информацию", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		
		if (choice == 0) {
			String pevHam = (String) model.getValueAt(row, 0); // порода, о котором меняется информ.
			String editedText = newText.getText(); // получаем текст из текстового поля
			
			log.warn("Проверка введенного пользователем текста. Может возникнуть ошибка");
			if(col == 0) {
				try {
					checkNewStr(editedText); // проверяем редактирование ячейки породы
					editElemDB(editedText, pevHam, 0); // редактируем
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
			}
			else if(col == 1) {
				try {
					checkNewInt(editedText); // проверяем редактирование ячейки цены
					editElemDB(editedText, pevHam, 1); // редактируем
				}
				catch(NotNumber ex) {
					JOptionPane.showMessageDialog(window, ex.getMessage());
					log.error("В строку 'цена' введен текст", ex);
				}
				catch(NullPointerException ex) {
					JOptionPane.showMessageDialog(window, ex.getMessage());
					log.error("Пустая строка", ex);
				}
			}
			else
				try {
					checkRules(editedText);
					editElemDB(editedText, pevHam, 2);
				}
				catch(NullPointerException ex) {
					JOptionPane.showMessageDialog(window, ex.getMessage());
					log.error("Пустая строка", ex);
				}
		}
		else log.debug("Пользователь нажал 'Отмена'");
	}
	
	/**
	 * Метод редактирования ячеек в базе данных
	 */
	private void editElemDB(String newText, String prevHam, int colNum) {
		allHamsters = getAllHamsters();
		for(Hamster newHam : allHamsters) { // по всем существующим породам
			if(newHam.getName().equals(prevHam)) { // нашли породу
				EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
			    EntityManager em = emf.createEntityManager();
			    em.getTransaction().begin();
			    if(colNum == 0) newHam.setName(newText); // если первый столбец, то меняем имя
			    else if (colNum == 1) newHam.setPrice(Integer.parseInt(newText)); // если второй, то цену
			    else newHam.setRules(newText); // если третий, то правила ухода
				em.merge(newHam);
				em.getTransaction().commit();
			    em.close();
			    break;
			}
		}
		model.setValueAt(newText, selectRow, selectColumn); // изменение текста в ячейке
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
        JLabel label1 = new JLabel("Порода:");
        JTextField textBreeder = new JTextField(20);
        JLabel label2 = new JLabel("Цена:");
        JTextField textSpecieses = new JTextField(20);
        JLabel label3 = new JLabel("Советы по уходу:");
        JTextField textRules = new JTextField(20);
        
        // Создание горизонтальной группы
        layout.setHorizontalGroup(layout.createSequentialGroup() 
                .addGroup(layout.createParallelGroup(LEADING) 
                		.addComponent(label1)
                		.addComponent(label2)
                		.addComponent(label3))
                .addGroup(layout.createParallelGroup(LEADING)
                        .addComponent(textBreeder) 
                        .addComponent(textSpecieses)
                        .addComponent(textRules))                		
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
                        .addComponent(label3) 
                        .addComponent(textRules))
        );
		
        // демострация диалогового окна и получение ответа пользователя
		int choice = JOptionPane.showOptionDialog(window, panel, "Добавить информацию", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		
		if (choice == 0) {
			String newHamster = textBreeder.getText(); // получаем текст
			String newPrice = textSpecieses.getText();
			String newRules = textRules.getText();
			try {
				log.warn("Проверка введенного пользователем текста. Может возникнуть ошибка");
				checkNewStr(newHamster); // проверяем введенный текст
				checkNewInt(newPrice); // проверяем введенный текст
				
				log.info("Добавление новой строки");
				addElemToDB(newHamster, newPrice, newRules);
			}
			catch(ExistElement ex) { 
				log.error("Добавление уже существующего разводчика", ex);
				JOptionPane.showMessageDialog(window, "Элемент " + newHamster + " уже существует!",
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
			catch (NotNumber ex) {
				JOptionPane.showMessageDialog(window, ex.getMessage());
				log.error("Введен текст в поле 'цифра'", ex);
			}
			
		
		}
		else log.debug("Пользователь нажал 'Отмена'");
	}
	
	/**
	 * Метод добавления строки в базу данных
	 * @param hamster
	 * @param price
	 * @param rules
	 */
	private void addElemToDB(String hamster, String price, String rules) {
		allHamsters = HamstersWindow.getAllHamsters(); // список всех пород
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
	    EntityManager em = emf.createEntityManager();
	    
	    em.getTransaction().begin();
		Hamster newHamster = new Hamster();
		newHamster.setName(hamster);
		newHamster.setNumber(0);
		newHamster.setPrice(Integer.parseInt(price));
		newHamster.setRules(rules);
		em.persist(newHamster);
		
		model.addRow(new Object[] {hamster, price, rules}); // добавление строки в таблицу
		em.getTransaction().commit();
	    em.close();
	}
	
	/**
	 * Метод поиска элементов в таблице
	 * @param speciesFilter - строка поиска
	 */
	private void hamsterSearch(JTextField speciesFilter) {
		String searchText = speciesFilter.getText(); // получаем текст из JTextBox
		searchText = searchText.toLowerCase();
		
		for(int i = 0; i < table.getRowCount(); i++) {
			Object cellValue = table.getValueAt(i, 0); // получаем значение в ячейке таблицы
			
			if (cellValue instanceof String) { // проверяем, что значение ячейки является строкой
                String cellText = (String)cellValue;
                cellText = cellText.toLowerCase();
				
                if (cellText.contains(searchText)) { // проверяем, содержит ли строка искомый элемент
                    table.getSelectionModel().setSelectionInterval(i, i); // отображаем строку с найденной ячейкой
                    return; // прерываем цикл, т.к. нашли первое совпадение
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Элемент не найден"); // выводим сообщение, если элемент не найден
	}
	
	
	/**
	 * Метод контроля добавления элементов в список
	 */
	private void checkNewStr(String hamsters) throws ExistElement, NotString, NullPointerException{
		// проверка на повтор породы
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.getValueAt(i, 0).equals(hamsters)) throw new ExistElement(); // если совпадает с 
																//существующим элементом списка, ошибка
		}
		
		if(hamsters != null) {
			if(hamsters.length() == 0) throw new NullPointerException(); // пустая строка
			
			for(int i = 0; i < hamsters.length(); i++) // проверка строки разводчика на наличие чисел
	            if(Character.isDigit(hamsters.charAt(i))) throw new NotString();
		} 
	}
	
	/**
	 * Метод контроля добавления чисел
	 * @param price - цена
	 * @throws NotNumber - введено не число
	 * @throws NullPointerException - пустая строка
	 */
	private void checkNewInt(String price) throws NotNumber, NullPointerException{
		if(price.length() == 0) throw new NullPointerException();
		for(int i = 0; i < price.length(); i++)
			if(Character.isDigit(price.charAt(i)) != true) throw new NotNumber();
	}
	
	/**
	 * Метод контроля добавления пустой строки в ячейку
	 * @param rules - правила ухода
	 * @throws NullPointerException - пустая строка
	 */
	private void checkRules(String rules) throws NullPointerException{
		if(rules.length() == 0) throw new NullPointerException(); // пустая строка
	}
	
	/**
	 * Метод контроля значения ввода названия породы при поиске
	 * @param species - текстовое поле для ввода
	 * @throws MyExceptionSearch - обработка строки со значением "Название породы"
	 * @throws NullPointerException - исключение пустой строки
	 */
	private void checkSearch(JTextField speciesFilter) throws MyExceptionSearch, NullPointerException, NotString {
		String sName = speciesFilter.getText();
		if(sName.contains("Название породы")) throw new MyExceptionSearch();
		
		if(sName.length() == 0) throw new NullPointerException();
		
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
	
	private class NotNumber extends Exception{
		public NotNumber() {
			super("Цена указывается только в числах");
		}
	}
}

