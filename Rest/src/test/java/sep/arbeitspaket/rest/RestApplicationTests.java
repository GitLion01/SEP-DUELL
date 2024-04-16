package sep.arbeitspaket.rest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sep.arbeitspaket.rest.adress.Adress;
import sep.arbeitspaket.rest.person.Person;
import sep.arbeitspaket.rest.person.PersonRepository;


import java.util.List;

@SpringBootTest
class RestApplicationTests {

	@Autowired
	private PersonRepository personRepository;

	@Test
	void contextLoads() {

		// Erstellen von Adressen
		Adress adress1 = new Adress("Musterstraße 1", "Berlin");
		Adress adress2 = new Adress("Beispielweg 2", "München");

		// Erstellen von Personen und Zuweisen von Adressen
		Person person1 = new Person("Max", "Mustermann", 1990, adress1);
		Person person2 = new Person("Anna", "Beispiel", 1985, adress2);

		// Speichern der Personen (und kaskadiertes Speichern der Adressen)
		personRepository.save(person1);
		personRepository.save(person2);

		// Filtern der Personen nach Geburtsjahr
		List<Person> filteredPersons = personRepository.findByBirthYearBetween(1990, 2005);

		// Berechnung des Durchschnittsalters
		double averageAge = personRepository.calculateAverageAge(filteredPersons);

		// Ausgabe des Durchschnittsalters
		System.out.println("Durchschnittsalter der Personen geboren zwischen 1990 und 2005: " + averageAge);


	}
}


