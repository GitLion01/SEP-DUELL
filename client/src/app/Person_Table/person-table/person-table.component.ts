import { Component, OnInit } from '@angular/core';
import {Person} from "../../Model/person";
import {PersonService} from "../../Service/person.service";

@Component({
  selector: 'app-person-table',
  templateUrl: './person-table.component.html',
  styleUrls: ['./person-table.component.scss']
})
export class PersonTableComponent implements OnInit
{
  persons: Person[]|undefined;
  searchMin:number=0;
  searchMax:number=0;
  searchCity:String="";

  constructor(private personService:PersonService) {}

  ngOnInit(): void
  {
    this.personService.findAll().subscribe(data=>{this.persons=data});
  }

  findPerson(searchMin: number, searchMax: number)
  {
      this.personService.findBetwen(searchMin,searchMax).subscribe(data=>{this.persons=data});
  }

  searchByCity(searchCity: String)
  {
      this.personService.findByCity(searchCity).subscribe(data=>{this.persons=data});
  }

  reset()
  {
    this.searchCity="";
    this.searchMin=0;
    this.searchMax=0;
    this.ngOnInit()
  }
}
