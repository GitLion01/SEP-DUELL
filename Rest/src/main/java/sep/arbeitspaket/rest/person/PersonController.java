package sep.arbeitspaket.rest.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sep.arbeitspaket.rest.adress.Adress;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class PersonController
{
    private final PersonService personService;

    //Dependency Injection: personService wird automatisch instanziiert,
    // dazu muss im PersonService @Service verwendet werden (Bean)
    @Autowired
    public PersonController(PersonService personService){
        this.personService = personService;
    }

    //Jede Methode braucht einen eindeutigen Pfad, damit für SpringBoot keine mehrdeutigkeiten entstehen
    //Anfragen werden an entsprechenden Pfad gesendet
    @GetMapping("/persons")
    public List<Person> getAllPersons(){
        return personService.getPersons();//getPersons kommt aus PersonService (nicht aus Repository)
    }

    @PostMapping("/persons")
    public Person addPerson(@RequestBody Person person){
        return personService.addPerson(person);
    }

    @GetMapping("/persons/city")
    public List<Person> getPersonByCity(@RequestParam String city){
        return personService.getPersonByCity(city);
    }

    @GetMapping("/persons/birthYear")
    public List<Person> getPersonByBirthYear(@RequestParam("minYear")int minYear, @RequestParam ("maxYear") int maxYear){
        return personService.getPersonByBirthYear(minYear, maxYear);
    }

    @GetMapping("/persons/average-age")
    public double getAverageAge(@RequestParam int minYear, @RequestParam int maxYear) {
        return personService.getAverageAgeByYearRange(minYear, maxYear);
    }


}
