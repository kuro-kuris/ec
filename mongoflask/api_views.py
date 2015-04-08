from flask import Blueprint
from flask_restful import Api, Resource, url_for, abort, reqparse
from mongoflask.brain import *
import json




# def abort_if_service_doesnt_exist(name):
#       if len(Service.objects(name = unicode(name))) == 0:
#           abort(404, message="Service number {} doesn't exist".format(name))


api_bp = Blueprint('api', __name__)
api = Api(api_bp)

parser = reqparse.RequestParser()
parser.add_argument('name', type=str)
parser.add_argument('latitude', type=float)
parser.add_argument('longitude', type=float)
parser.add_argument('orientation', type=float)

def simplify_stop_list(stop_list):
    simplified = []
    for stop in stop_list:
        wanted_keys = ['name', 'latitude', 'longitude']
        simplified.append(sub_dict(stop, wanted_keys))
    return simplified


class BusStops(Resource):

	
    def get(self, name):
    	#abort_if_service_doesnt_exist(name)
    	route = getServiceStops(unicode(name))
        response = simplify_stop_list(route)
        return {'stops' : response}


class NextStops(Resource):

    def get(self, name, latitude, longitude, orientation):
        stop_list = getServiceStops(name)
        route = getNextBusStops(name, latitude, longitude, orientation)
        response = simplify_stop_list(route['stops'])
        return {'stops' : response}

# Register resources

api.add_resource(BusStops, '/api/bus/<name>')
api.add_resource(NextStops, '/api/next/<name>+<latitude>+<longitude>+<orientation>')
