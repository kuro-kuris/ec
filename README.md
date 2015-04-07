# ediBus

## Installation

Install requirements from requirements.txt, it is recommended to install the packages in a virtualenv.

## Running the server

To run the server run $ python manage.py runserver to play around with the database you can run an interactive python session witht the database with $ python manage.py shell

## Step-by-step guide to run the project:

Initialise a virtualenvironment using the virtualenv command.
Activate virutalenv: 
```bash
. venv/bin/activate
```
Install requirments from the requirements.txt 
```bash
pip install -r requirements.txt
```
Now to run the server $ python manage.py runserver

## Playing around with the database

To play around with the database you can run:
```bash
python manage.py shell
```

## Testing the API

You can test the API with curl: $ curl $ curl http://127.0.0.1:5000/api/buses/NAME
Where NAME is the service name.
