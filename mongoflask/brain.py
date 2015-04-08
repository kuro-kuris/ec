# -*- coding: utf-8 -*-
import json
import sys
import math
from math import radians, cos, sin, asin, sqrt, atan2, degrees

with open('tfe_api/examples/tfe_getStops.txt') as data_file:    
	stops = json.load(data_file)


with open('tfe_api/examples/tfe_getServices.txt') as data_file:    
	services = json.load(data_file)

# create a dict with the key value pairs from wanted keys
def sub_dict(bigdict, wanted_keys):
	return {x: bigdict[x] for x in wanted_keys}

def getStop(stop_id):
	for stop in stops['stops']:
		if stop_id == stop['stop_id']:
			wanted_keys = ['name', 'stop_id', 'orientation', 'latitude', 'longitude']
			return sub_dict(stop, wanted_keys)
	return { 'message' : "stop_id not found: " + stop_id }

def getServiceStops(service_number):
	for service in services['services']:
		if unicode(service_number) == service['name']:
			routes = []
			for route in service['routes']:
				stop_ids = route['stops']
				stop_list = []
				for stop_id in stop_ids:
					stop = getStop(stop_id)
					stop.update({'destination' : route['destination']})
					stop_list.append(stop)
				routes = routes + (stop_list)
			return routes
	return { 'message' : "service_number not found" }

def getDirectedServiceStops(service_number, destination):
	stop_list = []
	for stop in getServiceStops(service_number):
		if destination == stop['destination']:
			stop_list.append(stop)
	return stop_list	



def getServiceDestinations(name):
	for service in services['services']:
		if unicode(name) == service['name']:
			destinations = []
			for route in service['routes']:
				destinations.append(route['destination'])
			return destination

def getServiceNumbers():
	numbers = []
	for service in services['services']:
		numbers.append(service['name'])
	return numbers

def haversine(pointA, pointB):
	lat1 = pointA[0]
	lon1 = pointA[1]
	lat2 = pointB[0]
	lon2 = pointB[1]
	"""
	Calculate the great circle distance between two points 
	on the earth (specified in decimal degrees) 
	"""
	# convert decimal degrees to radians 
	lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
	# haversine formula 
	dlon = lon2 - lon1 
	dlat = lat2 - lat1 
	a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
	c = 2 * asin(sqrt(a)) 
	m = 6367 * c * 1000
	return m

def similarOrientation(o1, o2, accuracy):
	if ((o2 % 360) < (o1 + accuracy) % 360) or ((o2 % 360) > (o1 - accuracy) % 360):
		return True
	else:
		return False

def calculate_initial_compass_bearing(pointA, pointB):
	"""
	Calculates the bearing between two points.

	The formulae used is the following:
		θ = atan2(sin(Δlong).cos(lat2),
				  cos(lat1).sin(lat2) − sin(lat1).cos(lat2).cos(Δlong))

	:Parameters:
	  - `pointA: The tuple representing the latitude/longitude for the
		first point. Latitude and longitude must be in decimal degrees
	  - `pointB: The tuple representing the latitude/longitude for the
		second point. Latitude and longitude must be in decimal degrees

	:Returns:
	  The bearing in degrees

	:Returns Type:
	  float
	"""
	if (type(pointA) != tuple) or (type(pointB) != tuple):
		raise TypeError("Only tuples are supported as arguments")
 
	lat1 = radians(pointA[0])
	lat2 = radians(pointB[0])
 
	diffLong = radians(pointB[1] - pointA[1])
 
	x = sin(diffLong) * (lat2)
	y = cos(lat1) * sin(lat2) - (sin(lat1)
			* cos(lat2) * cos(diffLong))
 
	initial_bearing = atan2(x, y)
 
	# Now we have the initial bearing but math.atan2 return values
	# from -180° to + 180° which is not what we want for a compass bearing
	# The solution is to normalize the initial bearing as shown below
	initial_bearing = degrees(initial_bearing)
	compass_bearing = (initial_bearing + 360) % 360
 
	return compass_bearing



# orientation is between 0 and 359 degrees where 0 and 359 degrees represent North
def getClosestStop(lat, lon, orientation, stops_list):
	orientation = float(orientation)
	our_position = (float(lat), float(lon))
	stops_in_correct_direction = []
	for stop in stops_list:
		stop_position = (stop['latitude'], stop['longitude'])
		compass_bearing = calculate_initial_compass_bearing(our_position, stop_position)
		if similarOrientation(orientation, compass_bearing, 75):
			stops_in_correct_direction.append(stop)
	# python maximum float value        
	minimum_distance = sys.float_info.max
	closest_stop = None
	for stop_in_correct_direction in stops_in_correct_direction:
		stop_position = (stop_in_correct_direction['latitude'], stop_in_correct_direction['longitude'])
		if haversine(our_position, stop_position) < minimum_distance:
			closest_stop = stop
			minimum_distance = haversine(our_position, stop_position)
	return closest_stop


def getRemainingStops(stop_id, stop_list):
	if len(stop_list) > 1:
		for i in range(len(stop_list)):
			if stop_id == stop_list[i]['stop_id']:
				return stop_list[i:]
		return stop_list

def getNextBusStops(name, latitude, longitude, orientation):
	stop_list = getServiceStops(name)
	# the upcoming bus stop closest to us
	next_stop = getClosestStop(latitude, longitude, orientation, stop_list)
	# stops on the correct bus route
	route_stops = getDirectedServiceStops(name, next_stop['destination'])
	remaining_stops = getRemainingStops(next_stop['stop_id'], route_stops)
	return {'stops' : remaining_stops}


# translate the JSON dict to SQL
def stopJSONtoSQL(stop_dict):
	stop = models.Stop(name = stop_dict['name'], stop_id = stop_dict['stop_id'],
		orientation = stop_dict['orientation'], latitude = stop_dict['latitude'],
		longitude = stop_dict['longitude']
	)



