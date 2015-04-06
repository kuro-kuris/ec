from flask import url_for
from mongoflask import db

class Stop(db.Model):
    name = db.Column(db.String(255))
    stop_id = db.Column(db.Integer, primary_key = True)
    orientation = db.Column(db.Integer)
    latitude = db.Column(db.Float)
    longitude = db.Column(db.Float)

    def __repr__(self):
        return self.name

class Route(db.Model):
    id = db.Column(db.Integer, primary_key = True)
    destination_id = db.Column(db.Integer, db.ForeignKey(Stop.stop_id))
    stops = db.relationship('Stop', backref = 'route')
    service_id = db.Column(db.Integer, db.ForeignKey('service.id'))

    def __repr__(self):
        return "Heading to " + self.destination

class Service(db.Model):
    id = db.Column(db.Integer, primary_key = True)
    name = db.Column(db.String(255), index = True, unique = True)
    routes = db.relationship('Route', backref = 'service_name', lazy = 'dynamic')

    def __repr__(self):
        return self.name
