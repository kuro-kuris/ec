from flask.ext.wtf import Form
from wtforms import StringField, IntegerField
from wtforms.validators import DataRequired

class InputForm(Form):
    service_number_input = StringField('service_number_input', validators=[DataRequired()])
    longitude = StringField('longitude');
    latitude = StringField('latitude');
    bearing = StringField('bearing');