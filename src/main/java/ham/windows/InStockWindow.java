package ham.windows;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import ham.entity.Hamster;
import ham.save.savePDF;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

/**
 * Класс экранной формы для отображения информации о количестве хомяков
 * @author Злата Счастливая
 *
 */
public class InStockWindow {	

	/** Окно с информацией о магазине */
	private JFrame window;
	
	/** Модель таблицы */
	private DefaultTableModel model;
	
	private String[] header;
	
	/** Таблица с информацией о магазине */
	private JTable table;
	
	/** Кнопка "Назад" */
	private JButton backButton;
	
	/** Панель с кнопкой "Назад" */
	private JPanel Panel;
	
	/** Панель инструментов */
	private JPanel toolPanel;
	
	/** Кнопка "Сохранить список" 
	private JButton saveButton;*/
	
	/** Кнопка "Создать отчет" */
	private JButton printButton;
	
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
	
	/** Координаты выделенной строки */
	private Point point;
	
	/** Выбранная строка */
	int selectRow;
	
	/** Выбранный столбец */
	int selectColumn;
	
	/** Объект для записи сообщений протоколирования */
	private static final Logger log = Logger.getLogger("InStockWindow.class");
	
	/** Конструктор класса */
	public InStockWindow() {
		log.info("Создание экранной формы");
		window = new JFrame("В наличии");
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
		printButton = new JButton("Отчет");
		//saveButton.setToolTipText("Сохранить список в файл"); // Настройка подсказок для кнопок
		printButton.setToolTipText("Сделать отчет");
		
		// Создание панели инструментов
		toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		toolPanel.setBorder(BorderFactory.createTitledBorder("Панель инструментов"));
		//toolPanel.add(saveButton); // Добавление кнопок на панель инструментов
		toolPanel.add(printButton);
		container.add(toolPanel);  // Размещение панели в контейнере
		
		// Создание таблицы с данными
		header = new String[]{"Название породы", "Количество"}; // заголовок таблицы
		model = new DefaultTableModel(null, header) { // модель таблицы
			@Override
			public boolean isCellEditable(int i, int i1) {
				if (i == selectRow && i1 == selectColumn) return true;
				else return false; // Запрет на редактирование ячеек таблицы
			}
		};
		getInfoFromDB();
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
		popupmenu.add(editButton);
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
	
	public void getInfoFromDB() {
		log.info("Загрузка данных о количестве хомяков из базы данных");
		// Создание фабрики EntityManager
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		// Создаем очередь пород из таблицы hamster
		Query qn = em.createQuery("SELECT e FROM Hamster e");
		// Преобразуем очередь в список элементов класса Hamster
		allHamsters = qn.getResultList();
		// Массив элементов таблицы
		String[] data = new String[2];
		if (allHamsters != null) {
			for(Hamster hamster : allHamsters) {
				data[0] = hamster.getName();
				data[1] = Integer.toString(hamster.getNumber());
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
					// вычисление координаты точки нажатия
					point = event.getPoint();
					
					// получаем номера строки и столбца, где произошло нажатие
					selectColumn = table.columnAtPoint(point);
					selectRow = table.rowAtPoint(point);
					
					// выделение строки
					table.setColumnSelectionInterval(selectColumn, selectColumn);
					table.setRowSelectionInterval(selectRow, selectRow);
					
					if(selectColumn != 0)
						log.info("Вызов контекстного меню");
						popupmenu.show(table, event.getX(), event.getY());	// отображение контекстного меню
				}
			}
			public void mouseReleased(MouseEvent event) {}
			public void mouseEntered(MouseEvent event) {}
			public void mouseExited(MouseEvent event) {}
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
	 * Метод редактирования ячейки таблицы
	 * @param row - выбранная строка
	 * @param col - выбранный столбец
	 */
	private void editElem(int row, int col) {
		// создание панели с полем для редактирования
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JTextField newText = new JTextField(20);
		JLabel label = new JLabel("Количество: ");
		panel.add(label);
		panel.add(newText);
		
		// демострация диалогового окна и получение ответа пользователя
		int choice = JOptionPane.showOptionDialog(window, panel, "Редактировать информацию", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		
		if (choice == 0) {
			String pevHam = (String) model.getValueAt(row, 0); // порода, о котором меняется информ.
			String editedText = newText.getText(); // получаем текст из текстового поля
		
			try {
				log.warn("Проверка введенного пользователем текста. Может возникнуть ошибка");
				checkNewInt(editedText); // проверяем редактирование ячейки породы
				editElemDB(editedText, pevHam);
			}
			catch(NullPointerException ex) {
				JOptionPane.showMessageDialog(window, ex.getMessage());
				log.error("Пустая строка", ex);
			}
			catch(NotNumber ex) {
				JOptionPane.showMessageDialog(window, ex.getMessage());
				log.error("В строку 'количество' введен текст", ex);
			}	
		}
		else log.debug("Пользователь нажал 'Отмена'");
	}
	
	private void editElemDB(String newText, String prevHam) {
		allHamsters = HamstersWindow.getAllHamsters();
		int num = Integer.parseInt(newText);
		
		for(Hamster newHam : allHamsters) { // по всем существующим породам
			if(newHam.getName().equals(prevHam)) { // нашли породу
				EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
			    EntityManager em = emf.createEntityManager();
			    em.getTransaction().begin();
			    newHam.setNumber(num);
				em.merge(newHam);
				em.getTransaction().commit();
			    em.close();
			    break;
			}
		}
		model.setValueAt(newText, selectRow, selectColumn); // изменение текста в ячейке
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
	
	
	private void checkNewInt(String price) throws NotNumber, NullPointerException{
		if(price.length() == 0) throw new NullPointerException();
		for(int i = 0; i < price.length(); i++)
			if(Character.isDigit(price.charAt(i)) != true) throw new NotNumber();
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
	
	/** Собственное исключение: ошибка при поиске элемента */
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
	
	private class NotNumber extends Exception{
		public NotNumber() {
			super("Количество указывается только в числах");
		}
	}
}

