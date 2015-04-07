import json
import fileconfig

with open('/var/www/ec/mongoflask/tfe_api/examples/tfe_getStops.txt') as data_file:    
    stops = json.load(data_file)

with open('/var/www/ec/mongoflask/tfe_api/examples/tfe_serviceUpdates.txt') as data_file:    
    services = json.load(data_file)

# create a dict with the key value pairs from wanted keys
def sub_dict(bigdict, wanted_keys):
	return {x: bigdict[x] for x in wanted_keys}

def getStop(stop_id):
	for stop in stops['stops']:
		if stop_id == stop['stop_id']:
			wanted_keys = ['name', 'stop_id', 'orientation', 'latitude', 'longitude', 'destinations']
			return sub_dict(stop, wanted_keys)
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



