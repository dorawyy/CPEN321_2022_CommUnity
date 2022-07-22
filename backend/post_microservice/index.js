const express = require('express');
const routes = require('./routes');
const axios = require('axios');
const db = require('./models');
const dotenv = require("dotenv")
const s2sToken = require('./config/config')["s2sToken"];

const OK = 200;
const CREATED = 201
const INTERNAL_SERVER_ERROR = 500;
const UNAUTHORIZED = 401;
const NOT_FOUND = 404;

dotenv.config({path: "../ports.env"});
dotenv.config();

const app = express();

app.use(express.json());
app.use(routes);

axios.defaults.headers = { s2sToken }
app.use(async (req, res, next) => {
  if (req.headers["s2sToken"] && req.headers["s2sToken"] == s2sToken) {
    next();
  } else {
    const token = req.headers['token'];
    await axios.post(`${process.env.USER_URL}/token/verify`, {token});
    if (user) {
      next();
    } else {
      res.status(UNAUTHORIZED).send("Unsuccessfull");
    }
  }
});

// app.use(express.urlencoded({ extended: true }));

db.sequelize.sync().then((req) => {}).catch(e => console.log(e));

const PORT = process.env.PORT || 8081;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}.`);
});

module.exports = { OK, CREATED, INTERNAL_SERVER_ERROR , UNAUTHORIZED, NOT_FOUND};