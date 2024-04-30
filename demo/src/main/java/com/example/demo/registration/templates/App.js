let express = require('express');
let app = express();
require( 'dotenv') . config()
let bodyParser = require('body-parser')

let app = express()

app.listen(8080)

/*let people = {
    'alice' : { name : 'Alice', age : 22},
    'bob' I: { name : 'Bob', age : 27},
'charlie' : {name : 'charlie', age : 25}*/

app.post('/registration', bodyParser.urlencoded({ extended: false }), (request, response, next)=>{

} )




