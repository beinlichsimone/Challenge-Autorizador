# challenge-autorizador

A Clojure library designed to create a bank account and process credit card transactions..

## Usage

run the program through the command: lein run

## Example of accepted inputs

{"account": {"active-card": true, "available-limit": 100}}

{"transaction": {"merchant": "Burger King", "amount": 20, "time": "2019-02-13T10:00:00.000Z"}}

{"transaction": {"merchant": "Habbib's", "amount": 90, "time": "2019-02-13T11:00:00.000Z"}}

{"transaction": {"merchant": "McDonald's", "amount": 30, "time": "2019-02-13T12:00:00.000Z"}}
