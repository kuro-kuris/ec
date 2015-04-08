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
		assert True == True

	def test_getClosestStop(self):
		stop_list = getServiceStops('44')
		position = stop_list[2]
		real_closest_stop = stop_list[3]
		assumed_closest_stop = getClosestStop(position['latitude'], position['longitude'], position['orientation'], stop_list)
		print "Assumed closest stop: "
		print assumed_closest_stop
		print "1"
		print stop_list[1]
		print "2"
		print stop_list[2]
		print "3"
		print stop_list[3]


		assert real_closest_stop == assumed_closest_stop



if __name__ == '__main__':
	unittest.main()