# ediBus

## Installation

Install requirements from requirements.txt, it is recommended to install the packages in a virtualenv.

## Running the server

To run the server run $ python manage.py runserver to play around with the database you can run an interactive ipython session witht he db with $ python manage.py shell .

## Playing around with the database

To play around with the database you can run $ python manage.py shell

Within the shell import the models and you can call Service.objects.get() or do whatever you want with the db.

## Testing the API

You can test the API with curl: $ curl $ curl http://127.0.0.1:5000/api/buses/<name>
Where name is the service name.
