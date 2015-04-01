from flask import url_for
from mongoflask import db

class Stop(db.Document):
    name = db.StringField(max_length=255, required=True)
    stop_id = db.IntField(required=True)
    latitude = db.FloatField(required=True)
    longitude = db.FloatField(required=True)

    def __unicode__(self):
        return self.name

class Service(db.Document):
    name = db.StringField(max_length=255, required=True)
    stops = db.ListField(db.EmbeddedDocumentField('Stop'))


    def get_absolute_url(self):
        return url_for('post', kwargs={"name": self.name})

    def __unicode__(self):
        return self.name

    meta = {
        'allow_inheritance': True,
        'indexes': ['name']
    }