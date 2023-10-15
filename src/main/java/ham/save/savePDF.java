package ham.save;

import java.awt.FileDialog;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.Font;


/**
 * Класс для сохранения данных в PDF-файл
 * @author Злата Счастливая
 */
public class savePDF extends JFrame {
	/** Окно выбора и сохранения файла */
	private FileDialog saveWindow;
	
	/** Имя каталога и название выбранного файла */
	private String fullName;
	
	/** Имя файла */
	private String fileName;
	
	/** Файл отчета */
	private Document doc;
	
	/** Шрифт заголовка */
	private Font fontH;
	
	/** Шрифт основного текста */
	private Font fontT;
	
	/** Таблица в документе */
	private PdfPTable table;
	
	/** Логгер класса **/ 
	private static final Logger log = Logger.getLogger(savePDF.class);
	
	/**
	 * Конструктор класса
	 * @param model - модель таблицы
	 * @param columnNames - названия столбцов таблицы
	 * @param tableName - название таблицы
	 */
	public savePDF(DefaultTableModel model, String[] columnNames, String tableName) {
		// Открытие окна для выбора файла
				log.info("Демонстрация экранной формы для выбора файла для сохранения");
				saveWindow = new FileDialog(this, "Сохранение данных", FileDialog.SAVE);
				saveWindow.setFile("*.pdf");
				saveWindow.setVisible(true);
						
				// Определение имени выбранного каталога и файла
				fullName = saveWindow.getDirectory() + saveWindow.getFile();
				fileName = saveWindow.getFile();
				if(fileName == null) {
					log.info("Пользователь нажал 'Отмена'");
					return;
				}
				
				try {
					// Проверка на корректность выбранного файла (расширение pdf, не пустое имя файла)
					log.warn("Выбор имени файла. Может возникнуть ошибка");
					checkFileName(fileName);
					
					log.info("Генерация и сохранение отчета по таблице '" + tableName + "' в файл " + fullName);
					log.warn("Создание документа. Может возникнуть ошибка при создании документа");
					// Создание корневого элемента
					doc = new Document(); 
					// создание экземпляра класса PdfWriter
					PdfWriter.getInstance(doc, new FileOutputStream(fullName));
					
					log.warn("Определение шрифтов таблицы. Может возникнуть ошибка");
					BaseFont bf = null;
					bf = BaseFont.createFont("/Windows/Fonts/comic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
					fontH = new Font(bf, 14);
					fontT = new Font(bf, 12);
					
					Phrase head = new Phrase("        " + tableName, fontH);
					// создание таблицы из n столбцов
					log.info("Создание таблицы");
					table = new PdfPTable(columnNames.length);
					table.getDefaultCell().setFixedHeight(25);
					
					// Добавление в таблицу заголовка
					for (int i = 0; i < columnNames.length; i++) {
						PdfPCell cell = new PdfPCell(new Phrase(columnNames[i], fontH));
						cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
						cell.setFixedHeight(30);
						table.addCell(cell);
					}
					// Добавление в таблицу данных
					for(int i = 0; i < model.getRowCount(); i++) { // Для всех строк
						for (int j = 0; j < columnNames.length; j++) // Для всех столбцов
							table.addCell(new Phrase((String)model.getValueAt(i,j), fontT));
					}
					
					log.info("Открытие документа для записи");
					doc.open();
					log.info("Добавление таблицы в документ");
					doc.add(head);
					doc.add(table);
					log.info("Закрытие документа");
					doc.close();
					
					JOptionPane.showMessageDialog(this, "Данные успешно сохранены в файл " + fullName);
				}
				catch(NoName ex) {
					log.error("Указано пустое имя файла", ex);
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
				}
				catch(WrongExtension ex) {
					log.error("Указано неверное расширение файла", ex);
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
				}
				catch (FileNotFoundException ex) {
					log.error("Файл шрифта не найден", ex);
					ex.printStackTrace();
				}
				catch (DocumentException ex) {
					log.error("Ошибка при создании документа", ex);
					ex.printStackTrace();
				}
				catch(IOException ex) { 
					log.error("Ошибка записи в файл", ex);
					ex.printStackTrace(); 
				} 
	}

	/** 
	 * Метод контроля значения имени файла
	 * @param fileName - имя файла
	 * @throws WrongExtension - исключение некорректного расширения файла
	 * @throws NoName - исключение пустого имени файла
	 */
	private void checkFileName(String fileName) throws WrongExtension, NoName {
		// неверное расширение
		if(!fileName.endsWith(".pdf")) throw new WrongExtension();
		// имя файла пустое (длина названия файла 4 - символы расширения)
		if(fileName.length() == 4) throw new NoName();
	}
	
	/**
	 * Собственное исключение: ошибка в выборе расширения файла
	 */
	private class WrongExtension extends Exception {
		/**
		 * Ошибка: файл иного расширения, кроме .pdf
		 */
		public WrongExtension() {
			super("Допускается сохранение только в формате *.pdf");
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
}
