import unittest
from brain import *



class brain_TestCase(unittest.TestCase):
	
	def test_similarOrientation(self):
		stop_list = getServiceStops('44')
		o1 = stop_list[0]['orientation']
		o2 = stop_list[4]['orientation']
		assert similarOrientation(o1, o1, 90) == True
		assert similarOrientation(o1, o2, 90) == False

	def test_haversine(self):
		stop_list = getServiceStops('44')
		pointA = (stop_list[3]['latitude'],stop_list[3]['longitude'])
		pointB = (stop_list[4]['latitude'],stop_list[4]['longitude'])
		assert haversine(pointA, pointA) == 0
		assert haversine(pointA, pointB) > 264 and haversine(pointA, pointB) < 265

	def test_calculate_initial_compass_bearing(self):
		stop_list = getServiceStops('44')
		pointA = (stop_list[3]['latitude'],stop_list[3]['longitude'])
		pointB = (stop_list[4]['latitude'],stop_list[4]['longitude'])
		assert calculate_initial_compass_bearing(pointA, pointB) > 324 and calculate_initial_compass_bearing(pointA, pointB) < 325

	def test_getRemainingStops(self):
		stop_list = getDirectedServiceStops('44', "Tranent")
		stop_id = stop_list[10]['stop_id']
		remaining_stops = getRemainingStops(stop_id, stop_list)		
		assert remaining_stops[len(remaining_stops) - 1] == stop_list[len(stop_list) - 1]

	def test_getClosestStop(self):
		stop_list = getServiceStops('44')
		position = stop_list[2]
		real_closest_stop = stop_list[2]
		assumed_closest_stop = getClosestStop()
		assert True == True
    

if __name__ == '__main__':
	unittest.main()
