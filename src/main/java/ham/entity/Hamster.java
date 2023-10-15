package ham.entity;

import java.util.*;

import jakarta.persistence.*;

/** Класс-сущность породы хомяка */
@Entity
@Table(name = "hamster")
public class Hamster {
	/** ID породы */
	@Id
	@Column(name = "id_hamster")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id_hamster;
	
	/** Название породы */
	@Column(name = "name")
	private String name; 
	
	/** Количество */
	@Column(name = "number")
	private int number;
	
	/** Цена хомяка */
	@Column(name = "price")
	private int price;
	
	/** Правила ухода за животным */
	@Column(name = "care_rules")
	private String care_rules; 
	
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@JoinTable(name="species",
		       joinColumns = @JoinColumn(name = "id_ham", referencedColumnName="id_hamster"), 
		       inverseJoinColumns = @JoinColumn(name = "id_breed", referencedColumnName="id_breeder"))
	private Set<Breeder> breederList = new HashSet<Breeder>(); 
	
	/** Конструктор класса */
	public Hamster(String name, int price, String care_rules) {
		this.name = name;
		this.price = price;
		this.care_rules = care_rules;
	}
	
	/** Дефолтный конструктор класса */
	public Hamster() {
		this.name = "";
		this.price = 0;
		this.care_rules = "";
	}
	
	/** Геттер ID породы */
	public int getID() {
		return id_hamster;
	}
	
	/** Геттер названия породы */
	public String getName() {
		return name;
	}
	
	/** Геттер количества хомяков */
	public int getNumber() {
		return number;
	}
	
	/** Геттер цены хомяка */
	public int getPrice() {
		return price;
	}
	
	/** Геттер правил ухода за хомяком */
	public String getRules() {
		return care_rules;
	}
	
	/** 
	 * Сеттер названия породы хомяка 
	 * @param newName - новое название породы
	 */
	public void setName(String newName) {
		this.name = newName;
	}
	
	/** 
	 * Сеттер цены хомяка 
	 * @param newPrice - новая цена
	 */
	public void setNumber(int num) {
		this.number = num;
	}
	
	/** 
	 * Сеттер цены хомяка 
	 * @param newPrice - новая цена
	 */
	public void setPrice(int newPrice) {
		this.price = newPrice;
	}
	
	/** 
	 * Сеттер правил ухода за хомяком 
	 * @param newRules - новые правила ухода
	 */
	public void setRules(String newRules) {
		this.care_rules = newRules;
	}
	
	/** 
	 * Добавление связи порода - разводчк
	 * @param newB - ссылка на разводчика
	 
	public void addBreed(Breeder newB) {
		breederList.add(newB);
	}*/
	 /**
	  * Удаление связи порода-разводчики
	  
	public void setBreedListToNull() {
		this.breederList.clear();
	}*/
	
	public void deleteElem() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hamshop_persistence");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Query qSpicies = em.createQuery("DELETE e FROM spicies e WHERE e.id_ham =: id_ham");
		qSpicies.setParameter("id_ham", id_hamster);
		qSpicies.executeUpdate();
		em.remove(this);
		em.getTransaction().commit();
		em.close();
	}
}
