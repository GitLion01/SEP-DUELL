package sep.arbeitspaket.rest.person;

import sep.arbeitspaket.rest.adress.Adress;

import javax.persistence.*;


@Entity
public class Person
{
    // Todo
    // Klasse um Attribute und Methoden ergänzen
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private int birthYear;

    //Fremdschlüsselverweis auf Tabelle "Adress" (Mehrere Personen gehören zu einer Adresse = ManyToOne)
   @ManyToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "Person_Adress")
   private Adress adress;

    public Person() {
    }

    //keine ID als Parameter notwendig. Wird von DB generiert
    public Person(String firstName, String lastName, int birthYear, Adress adress) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
        this.adress = adress;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public Adress getAdress() {
        return adress;
    }

    public void setAdress(Adress adress) {
        this.adress = adress;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthYear='" + birthYear + '\'' +
                ", adress=" + adress +
                '}';
    }
}
