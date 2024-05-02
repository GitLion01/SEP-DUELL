


const express = require('express');
const cors = require('cors');

const app = express();


app.use(cors());


app.use(express.json());


app.post("*", (req, res) => {

    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
    res.header('Access-Control-Allow-Headers', 'Content-Type'); //

});


app.listen(8080, () => {
    console.log('Server läuft auf Port 8080');
});
