import {Adress} from "./adress";

export class Person
{
    id:String|undefined;
    firstName:String|undefined;
    lastName:String|undefined;
    yearOfBirth:String|undefined;
    adresse:Adress=new Adress();
}
