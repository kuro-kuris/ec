from flask import Blueprint
from flask_restful import Api, Resource, url_for, abort, reqparse
from mongoflask.models import Service, Route, Stop
from mongoflask import db
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

class BusStops(Resource):

	
    def get(self, name):
    	#abort_if_service_doesnt_exist(name)
    	routes = getServiceStops(unicode(name)) 
        response = {}
        response.update({'Route' : routes})
        return response


class NextStops(Resource):

    def get(self, name, latitude, longitude, orientation):
        stop_list = getServiceStops(name)
        response = getNextBusStops(name, latitude, longitude, orientation)
        response.update({'latitude' : latitude, 'longitude' : longitude, 'orientation' : orientation})
        return response

# Register resources

api.add_resource(BusStops, '/api/bus/<name>')
api.add_resource(NextStops, '/api/next/<name>+<latitude>+<longitude>+<orientation>')
