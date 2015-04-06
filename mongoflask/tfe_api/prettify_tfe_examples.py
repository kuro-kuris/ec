from tfe_api import *
from time import time

def pretty_json_response(filename, response):
	path = os.path.join("examples", filename)

	f = open(path, "w")

	r_json = response.json()

	json.dump(r_json, f, sort_keys = True, indent = 4, ensure_ascii = False)

	f.close()

def prettify():

	pretty_json_response("tfe_getStops.txt", getStops())

	pretty_json_response("tfe_serviceUpdates.txt", serviceUpdates())

	pretty_json_response("tfe_timeTable.txt", timeTable(36234964))

	pretty_json_response("tfe_journeys.txt", journeys('X12'))

	date = time() + 2 * 24 * 60 * 60
	pretty_json_response("tfe_stop2stop.txt", stop2stop(36236495, 36232896, date, 120))

	pretty_json_response("tfe_disruptions.txt", disruptions())

	pretty_json_response("tfe_live_bus_location.txt", live_bus_location())

prettify()