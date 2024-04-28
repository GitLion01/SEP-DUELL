import { Component, OnInit } from '@angular/core';
import {Person} from "../../Model/person";
import {PersonService} from "../../Service/person.service";

@Component({
  selector: 'app-person-form',
  templateUrl: './person-form.component.html',
  styleUrls: ['./person-form.component.scss']
})
export class PersonFormComponent implements OnInit {

  person:Person;
  constructor(private personService:PersonService) {this.person=new Person()}

  ngOnInit(): void {
  }

  onSubmit()
  {
    this.personService.save(this.person).subscribe(result=>this.reloadPage());
  }

  private errorWithForm()
  {
    alert("Problem beim erstellen neuer Person überpüfe deine eingabe");
  }

  private reloadPage()
  {
    window.location.reload();
  }
}
