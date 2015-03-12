import json
import requests
import os

API_KEY = os.environ.get("TFE_API_KEY")

general_url = 'https://tfe-opendata.com/api/v1/'

essential_parameters = dict(
	key = API_KEY
)

def getStops():

	url = general_url + 'stops'

	parameters = essential_parameters

	response = requests.get(url = url, params = parameters)

	return response

def serviceUpdates():

	url = general_url + 'services'

	parameters = essential_parameters

	response = requests.get(url = url, params = parameters)

	return response

def timeTable(stop_id):

	url = general_url + 'timetables/' + str(stop_id)

	parameters = dict(
		stop_id = stop_id
	)

	parameters.update(essential_parameters)

	response = requests.get(url = url, params = parameters)

	return response

def journeys(service_name):

	url = general_url + '/journeys/' + service_name

	parameters = dict(
		service_name = unicode(service_name)
	)

	parameters.update(essential_parameters)

	response = requests.get(url = url, params = parameters)

	return response

def stop2stop(start_stop_id, end_stop_id, date, duration):

	url = general_url + '/stoptostop-timetable/'

	parameters = dict(
		start_stop_id = start_stop_id,
		finish_stop_id = end_stop_id,
		date = date,
		duration = duration
	)

	parameters.update(essential_parameters)

	response = requests.get(url = url, params = parameters)

	return response

def disruptions():

	url = general_url + '/status'

	parameters = essential_parameters

	response = requests.get(url = url, params = parameters)

	return response

def directions(initial_location, destination_location, date, time_mode):

	url = general_url + '/directions/'

	parameters = dict(
		start = initial_location,
		finish = destination_location,
		date = date,
		time_mode = unicode(time_mode)
	)

	parameters.update(essential_parameters)

	response = requests.get(url = url, params = parameters)

	return response

def live_bus_location():

	url = general_url + 'vehicle_locations'

	parameters = essential_parameters

	response = requests.get(url = url, params = parameters)

	return response



