package sep.arbeitspaket.rest.adress;


import sep.arbeitspaket.rest.person.Person;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Adress
{
    // Todo
    // KLasse um Attribute und Methoden erweitern
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adressID;
    private String city;
    private String street;


    public Adress() {
    }

    public Adress(String city, String street) {
        this.city = city;
        this.street = street;
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public String toString() {
        return "Adress{" +
                "adressID=" + adressID +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                '}';
    }
}
