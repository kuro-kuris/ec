import json

file = open("tfe_getStops.txt").read()

data = json.loads(json_data)


def getStops(service_number)
for stop in data['stops']:
	if unicode(service_number) in stop['services']
		
	