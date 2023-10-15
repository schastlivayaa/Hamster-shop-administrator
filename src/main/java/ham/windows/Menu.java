package ham.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class Menu {
	/** Окно главного меню */
	private JFrame window;
	
	/** Кнопка для открытия окна с информацией о разводчиках */
	private JButton Breeders;
	
	/** Кнопка для открытия окна с информацией о хомяках */
	private JButton Hamsters;
	
	/** Кнопка для открытия окна с отчетом о состоянии магазина */
	private JButton InStock;
	
	/** Кнопка для открытия окна с информацией о магазине */
	private JButton Shop;
	
	/** Конструктор класса */
	public Menu() {
		// Создание окна
		window = new JFrame("Главное меню");
		window.setSize(400, 300);
		window.setLocation(500, 200);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Устанавливаем цвет всех кнопок приложения
		UIManager.put("Button.background", Color.LIGHT_GRAY);
		
		// Создание кнопок
		Breeders = new JButton("Информация о разводчиках");
		Breeders.setAlignmentX(Component.CENTER_ALIGNMENT);
		Hamsters = new JButton("Информация о хомяках");
		Hamsters.setAlignmentX(Component.CENTER_ALIGNMENT);
		InStock = new JButton("В наличии");
		InStock.setAlignmentX(Component.CENTER_ALIGNMENT);
		Shop = new JButton("О магазине");
		Shop.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		Container container = window.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		// Добавление кнопок в контейнер
		container.add(Box.createVerticalStrut(50));
		container.add(Breeders);
		container.add(Box.createVerticalStrut(10));
		container.add(Hamsters);
		container.add(Box.createVerticalStrut(10));
		container.add(InStock);
		container.add(Box.createVerticalStrut(10));
		container.add(Shop);
		
		// Слушатели для кнопок. Выполняется вызов соответствующих экранных форм
		Breeders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new BreedersWindow();
			}
		});
		Hamsters.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				new HamstersWindow();
			}
		});
		InStock.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				new InStockWindow();
			}
		});
		Shop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				new ShopWindow();
			}
		});
		
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
	* Создание и отображение экранной формы
	* @param args - строковые параметры, передаваемые программе
	*/
	public static void main(String[] args) {
		new Menu().show();
	}
}