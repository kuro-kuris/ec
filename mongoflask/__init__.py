from flask import Flask
from flask.ext.restful import reqparse, abort, Api, Resource
from flask.ext.sqlalchemy import SQLAlchemy
from flask.ext.migrate import Migrate

app = Flask(__name__)
app.config.from_object('config')
db = SQLAlchemy(app)
migrate = Migrate(app, db)


# To avoid circular imports
def register_views(app):
    from mongoflask.views import services
    app.register_blueprint(services)

register_views(app)

def register_api_views(app):
	from mongoflask.api_views import api_bp
	app.register_blueprint(api_bp)

register_api_views(app)


if __name__ == '__main__':
	app.run()
