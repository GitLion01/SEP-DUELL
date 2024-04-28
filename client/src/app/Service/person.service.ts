import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Person} from "../Model/person";

@Injectable({
  providedIn: 'root'
})
export class PersonService {
  personURL:string;
  constructor(private http:HttpClient) {this.personURL='http://localhost:8080/persons'}

  findAll():Observable<Person[]>
  {
    return this.http.get<Person[]>(this.personURL)
  }

  findBetwen(yearMin:number,yearMax:number)
  {
    return this.http.get<Person[]>(this.personURL+"/birthYear/"+yearMin+"/"+yearMax)
  }

  save(person:Person)
  {
    return  this.http.post<Person>(this.personURL,person);
  }

  findByCity(city:String)
  {
    return this.http.get<Person[]>(this.personURL+"/"+city);
  }
}


