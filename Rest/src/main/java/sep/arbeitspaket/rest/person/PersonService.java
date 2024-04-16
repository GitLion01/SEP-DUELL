package sep.arbeitspaket.rest.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sep.arbeitspaket.rest.adress.Adress;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional
    public Person addPerson(Person person) {
        return personRepository.save(person);
    }

    @Transactional(readOnly = true)
    public List<Person> getPersons() {
        return personRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Person> getPersonByCity(String city) {
        return personRepository.findByAdressCity(city);
    }

    @Transactional(readOnly = true)
    public List<Person> getPersonByBirthYear(int minYear, int maxYear) {
        return personRepository.findByBirthYearBetween(minYear, maxYear);
    }

    @Transactional(readOnly = true)
    public double getAverageAgeByYearRange(int minYear, int maxYear) {
        List<Person> persons = personRepository.findByBirthYearBetween(minYear, maxYear);
        return personRepository.calculateAverageAge(persons);
    }




}
