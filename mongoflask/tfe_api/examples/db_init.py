import json
from mongoflask import db, models

with open('tfe_getStops.txt') as data_file:    
    stops = json.load(data_file)

with open('tfe_serviceUpdates.txt') as data_file:    
    services = json.load(data_file)

def getStop(stop_id):
	for stop in stops['stops']:
		if stop_id == stop['stop_id']:
			return stop
	return { 'message' : "stop_id not found." }

def getServiceStops(service_number):
	for service in services['services']:
		if unicode(service_number) == service['name']:
			routes = []
			for route in service['routes']:
				stop_ids = service['routes'][0]['stops']
				stops = []
				for stop_id in stop_ids:
					stops.append(getStop(stop_id))
				routes.append(stops)
			return routes
	return { 'message' : "service_number not found" }

def getServiceNumbers():
	numbers = []
	for service in services['services']:
		numbers.append(service['name'])


# translate the JSON dict to SQL
def stopJSONtoSQL(stop_dict):
	stop = models.Stop(name = stop_dict['name'], stop_id = stop_dict['stop_id'],
		orientation = stop_dict['orientation'], latitude = stop_dict['latitude'],
		longitude = stop_dict['longitude']
	)



service_numbers = getServiceNumbers()
for service_number in service_numbers:
	service = models.Service(name = service_number)
	stops = getServiceStops(service_number)


