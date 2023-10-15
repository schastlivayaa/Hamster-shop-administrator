package ham.entity;

import java.util.*;

import jakarta.persistence.*;

/** Класс-сущность разводчика */
@Entity
@Table(name="breeder")
public class Breeder {
	/** ID разводчика в БД */
	@Id
	@Column(name = "id_breeder")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id_breeder;
	
	/** ФИ разводчика */
	@Column(name="name")
	private String name;
	
	/** Ссылка на породы хомяков, которыми занимается разводчик */
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinTable(name="species",
		       joinColumns = @JoinColumn(name = "id_breed", referencedColumnName="id_breeder"), 
		       inverseJoinColumns = @JoinColumn(name = "id_ham", referencedColumnName="id_hamster"))
	private Set<Hamster> hamstersList = new HashSet<Hamster>();
	
	/** Конструктор класса */
	public Breeder(String name) {
		this.name = name;
	}
	
	/** Дефолтный конструктор класса */
	public Breeder() {
		this.name = "";
	}
	
	public int getID() {
		return id_breeder;
	}
	
	/** Геттер ФИ разводчика */
	public String getName() {
		return name;
	}
	
	/**
	 * Сеттер ФИ разводчика
	 * @param newName - новое ФИ разводчика
	 */
	public void setName(String newName) {
		this.name = newName;
	}
	
	/** 
	 * Добавление связи разводчк - порода
	 * @param newH - ссылка на породу хомяка
	 */
	public void addHam(Hamster newH) {
		hamstersList.add(newH);
	}
	
	/**
	 * Геттер пород хомяков
	 * @return hamstersList
	 */
	public Set<Hamster> getHams() {
		return hamstersList;
	}
	
	/**
	 * Очистка списка связей разводчик-породы
	 */
	public void setHamListToNull() {
		this.hamstersList.clear();;
	}
	
	/**
	 * Проверка на наличие связи разводчик-порода
	 * @param hamName - название породы
	 * @return
	 */
	public boolean checkHamster(String hamName) {
		boolean flag = false;
		for(Hamster h : hamstersList) {
			if (h.getName().equals(hamName)) flag = true;
		}
		return flag;
	}
	
	/**
	 * Получить строку с породами хомяков в виде
	 * порода;порода;порода
	 * @throws StringIndexOutOfBoundsException
	 */
	public String getHamsStr() throws StringIndexOutOfBoundsException {
		String hamsStr = "";
		for(Hamster h : hamstersList) {
			String hamName = h.getName();
			hamsStr = hamsStr + hamName + ";";
		}
		String result = null;
		return hamsStr.substring(0, hamsStr.length()-1);
	}
}
