from flask import Flask
from flask.ext.restful import reqparse, abort, Api, Resource
from flask.ext.sqlalchemy import SQLAlchemy
from flask.ext.migrate import Migrate
import jsonSocket

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

class MyServer(jsonSocket.ThreadedServer):
    def __init__(self):
        super(MyServer, self).__init__()
        self.timeout = 2.0
 
    def _processMessage(self, obj):
        """ virtual method """
        if obj != '':
            if obj['message'] == "new connection":
                pass


if __name__ == '__main__':
	c = MyServer()
	c.start()
	app.run()
