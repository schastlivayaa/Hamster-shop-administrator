package ham.save;

import java.awt.FileDialog;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Класс для сохранения данных в файл
 * @author Злата Счастливая
 */
public class FileSave extends JFrame{
	/** Окно выбора и сохранения файла */
	private FileDialog saveWindow;
	/** Имя каталога и название выбранного файла */
	private String fullName;
	/** Имя файла */
	private String fileName;
	/** Поток записи данных в файл */
	private BufferedWriter writer;
	/** Файл, куда производится сохранение */
	private Document doc;
	
	/**
	 * Конструктор класса
	 * @param str - текст заголовка диалогового окна
	 * @param model - модель сохраняемой таблицы 
	 */
	public FileSave(String str, DefaultTableModel model) {
		// Открытие окна для выбора файла
		saveWindow = new FileDialog(this, str, FileDialog.SAVE);
		saveWindow.setVisible(true);
		
		// Определение имени выбранного каталога и файла
		fullName = saveWindow.getDirectory() + saveWindow.getFile();
		fileName = saveWindow.getFile();
		if(fileName == null) return; // Если пользователь нажал «отмена»
		
		// Сохранение списка в файл
		try {
			// Проверка на корректность выбранного файла (расширение txt или xml, не пустое имя файла)
			checkFullName(fullName, fileName);
			
			// Вызов соответствующей функции сохранения данных в файл
			if(fullName.endsWith(".txt")) saveTXT(model);
			else saveXML(model);
			
			JOptionPane.showMessageDialog(this, "Данные успешно сохранены в файл " + fullName);
		}
		catch(NoName ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(WrongExtension ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	
	/**
	 * Сохранение данных в файл .txt
	 * @param model - модель сохраняемой таблицы
	 */
	private void saveTXT(DefaultTableModel model) {
		try {
			writer = new BufferedWriter (new FileWriter(fullName));
			for (int i = 0; i < model.getRowCount(); i++) { // Для всех строк
				for (int j = 0; j < model.getColumnCount(); j++) { // Для всех столбцов
					writer.write((String)model.getValueAt(i, j)); // Записать значение из ячейки
					if (j < 5) writer.write(";"); // Записать символ перевода каретки
				}
				writer.write("\r\n");
			}
			writer.close();
		}
		catch(IOException e) { e.printStackTrace(); } // Ошибка записи в файл
	}
	
	/**
	 * Сохранение данных в файл .xml
	 * @param model - модель сохраняемой таблицы
	 */
	private void saveXML(DefaultTableModel model) {
		// Создание парсера и файла
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// Создание пустого документа
			doc = builder.newDocument();
		} catch (ParserConfigurationException e) { e.printStackTrace();}
		
		// Создание корневого элемента hamlist и добавление его в документ
		Node hamlist = doc.createElement("hamlist");
		doc.appendChild(hamlist);
		// Создание дочерних элементов hamster и присвоение значений атрибутам 
		for(int i=0; i < model.getRowCount(); i++) {
			Element hamster = doc.createElement("hamster");
			hamlist.appendChild(hamster);
			hamster.setAttribute("species", (String)model.getValueAt(i, 0));
			hamster.setAttribute("number", (String)model.getValueAt(i, 1));
			hamster.setAttribute("price", (String)model.getValueAt(i, 2));
			hamster.setAttribute("careRules", (String)model.getValueAt(i, 3));
			hamster.setAttribute("breeder", (String)model.getValueAt(i, 4));	
		}
		
		// Сохранение полученного списка в файл
		try {
			// Создание преобразователя документа
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			// Создание файла для записи документа
			FileWriter fw = new FileWriter(fullName);
			// Запись документа в файл
			trans.transform(new DOMSource(doc), new StreamResult(fw));
		}
		// Ошибка создания XML-преобразователя
		catch (TransformerConfigurationException e) { e.printStackTrace(); }
		// Ошибка работы XML-преобразователя
		catch (TransformerException e) { e.printStackTrace(); }
		// Ошибка ввода/вывода
		catch (IOException e) { e.printStackTrace(); }
	}
	
	/**
	 * Собственное исключение: ошибка в выборе расширения файла
	 */
	private class WrongExtension extends Exception {
		/**
		 * Ошибка: файл иного расширения, кроме .txt или .xml
		 */
		public WrongExtension() {
			super("Файл сохраняется только в формате *.txt или *.xml");
		}
	}
	
	/**
	 * Собственное исключение: ошибка в названии файла
	 */
	private class NoName extends Exception {
		/**
		 * Ошибка: имя файла пустое
		 */
		public NoName() {
			super("Введите название файла");
		}
	}
	/**
	 * 
	 * Контроль значения ввода имени файла
	 * @param fullName - имя выбранного каталога и файла
	 * @param fileName - имя файла
	 * @throws WrongExtension - исключение некорректного расширения файла
	 * @throws NoName - исключение пустого имени файла
	 */
	private void checkFullName(String fullName, String fileName) throws WrongExtension, NoName {
		// неверное расширение
		if(!fullName.endsWith(".txt") && !fullName.endsWith(".xml")) throw new WrongExtension();
		// имя файла пустое (длина названия файла 4 - символы расширения)
		if(fileName.length() == 4) throw new NoName();
	}
	
}
