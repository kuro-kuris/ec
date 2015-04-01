from flask import Blueprint
from flask_restful import Api, Resource, url_for, abort
from mongoflask.models import Service, Stop


api_bp = Blueprint('api', __name__)
api = Api(api_bp)

def abort_if_service_doesnt_exist(name):
		if len(Service.objects(name = unicode(name))) == 0:
			abort(404, message="Service number {} doesn't exist".format(name))

class BusStops(Resource):

	
    def get(self, name):
    	abort_if_service_doesnt_exist(name)
    	service = Service.objects(name = unicode(name)).get()
        response = {}
        for stop in service.stops:
        	response.update({'Name' : stop.name})
        return response

# Register resources

api.add_resource(BusStops, '/api/bus/<name>')
