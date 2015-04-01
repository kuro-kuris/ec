import unittest
from tfe_api import *
from time import time



class tfe_apiTestCase(unittest.TestCase):
	
	def test_getStops(self):
		r = getStops()
		assert r.status_code == 200

	def test_serviceUpdates(self):
		r = serviceUpdates()
		assert r.status_code == 200

	def test_timeTable(self):
		r = timeTable(58232684)
		assert r.status_code == 200

	def test_journeys(self):
		r = journeys('X12')
		assert r.status_code == 200

	def test_stop2stop(self):
		date = time() + 2 * 24 * 60 * 60
		r = stop2stop(36236495, 36232896, date, 120)
		assert r.status_code == 200

	def test_disruptions(self):
		r = disruptions()
		assert r.status_code == 200

	def test_directions(self):
		start = (55.31112, -3.12797)
		finish = (55.98071, -3.19447)
		date = time() + 2 * 24 * 60 * 60
		time_mode = 'LeaveAfter'
		r = directions(start, finish, date, time_mode)
		assert r.status_code == 200

	def test_live_bus_location(self):
		r = live_bus_location()
		assert r.status_code == 200

if __name__ == '__main__':
	unittest.main()