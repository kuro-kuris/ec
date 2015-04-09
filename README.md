# ediBus

## Installation

Install requirements from requirements.txt, it is recommended to install the packages in a virtualenv.

## Running the server

To run the server run $ python manage.py runserver to play around with the database you can run an interactive python session witht the database with $ python manage.py shell

## Step-by-step guide to run the project locally:

Initialise a virtualenvironment using the virtualenv command.
```bash
virtualenv --distribute --python=/usr/bin/python2.7 edibus
```

Activate virutalenv: 
```bash
. venv/bin/activate
```
Install requirments from the requirements.txt 
```bash
pip install -r requirements.txt
```
Now to run the server
```bash
python manage.py runserver
```

## Playing around with the database

To play around with the database you can run:
```bash
python manage.py shell
```

## Testing the API

You can test the API with curl: $ curl $ curl http://127.0.0.1:5000/api/buses/NAME
Where NAME is the service name.

## Deploying the server

Connect to the DigitalOcean droplet server through SSH or the DigitalOcean browser-based terminal, login as root.
Pull latest backend code, modifying directory paths to absolute ones:
```
cd /var/www/ec/
git pull
nano /var/www/ec/mongoflask/brain.py //Change 'tfe_api/...txt' to '/var/www/ec/mongoflask/tfe_api...'
sudo service apache2 restart
```
The server is accessible from http://178.62.140.115/ , with API calls in the format
http://178.62.140.115/api/next/servicename+latitude+longitude+bearing
i.e.
http://178.62.140.115/api/next/34+34.5754+-3.234+127
