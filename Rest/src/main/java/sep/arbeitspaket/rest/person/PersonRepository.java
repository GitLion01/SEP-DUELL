package sep.arbeitspaket.rest.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>
{
    //Spring Data JPA erwartet, dass die Parameter in Methodensignaturen
    // als String deklariert werden, weil die Parameterwerte aus den HTTP-Anfragen stammen,
    // die normalerweise als Strings übermittelt werden
    List<Person> findByBirthYearBetween(int minYear, int maxYear);
    List<Person> findByAdressCity(String city);

    @Query("SELECT AVG(YEAR(CURRENT_DATE()) - p.birthYear) FROM Person p WHERE p IN :persons")
    double calculateAverageAge(@Param("persons") List<Person> persons);


}
