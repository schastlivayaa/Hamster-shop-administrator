package ham.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ShopWindow {
	/** Окно с информацией о магазине */
	private JFrame window;
	
	/** Модель таблицы */
	private DefaultTableModel model;
	
	/** Таблица с информацией о магазине */
	private JTable table;
	
	/** Кнопка "Назад" */
	private JButton backButton;
	
	/** Конструктор класса */
	public ShopWindow() {
		// Создание окна
		window = new JFrame("О магазине");
		window.setSize(600, 300);
		window.setLocation(450, 200);
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// Создание кнопки возврата в главное меню
		backButton = new JButton("Назад");
		// Блок прослушивания кнопки "Назад"
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				window.dispose();
			}
		});
		// Добавление кнопки к панели
		JPanel Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Panel.add(backButton);
		// Размещение панели
		window.add(Panel, BorderLayout.NORTH);
		
		String[] columnNames = {"1", "2"};
		String[][] data = {{"Название", "Пушистики"},
				{"Адрес", "г. Санкт-Петербург, ул. Хомячковая 1"},
				{"Директор", "Счастливая Злата Владленовна"}
		};
		
		// Модель таблицы
		model = new DefaultTableModel(data, columnNames) {
			// Запрет на редактирование ячеек таблицы
 			@Override
 			public boolean isCellEditable(int i, int i1) {
 				return false;
 			}
		};
		// создаем рендерер для ячеек первого столбца
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setBackground(Color.LIGHT_GRAY); // устанавливаем серый фон
		renderer.setFont(new Font("Arial", Font.BOLD, 12)); // шрифт
		// Создание таблицы
		table = new JTable(model);
		table.setTableHeader(null); // скрываем заголовок
		table.setCellSelectionEnabled(false); // запрещаем выделение ячеек
		table.setRowHeight(30); // высота ячеек
		table.getColumnModel().getColumn(0).setMaxWidth(100); // ширина первого столбца
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer); // установить рендерер для первого столбца
		JScrollPane scroll = new JScrollPane(table);
		
		// Размещение таблицы с данными
		window.add(scroll, BorderLayout.CENTER);
		
		// отображение экранной формы
		show();
	}
	
	/**
	 * Демонстрация экранной формы
	 */
	public void show() {
		window.setVisible(true);
	}
}
