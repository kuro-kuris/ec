from flask import Blueprint, request, redirect, flash, render_template, url_for
from flask.views import MethodView
from forms import InputForm

services = Blueprint('services', __name__, template_folder = 'templates')

class InputView(MethodView):

	@services.route('/', methods= ['GET', 'POST'])
	def my_form():
		form = InputForm()
		flash('bus is ="%s", longitude ="%s", latitude="%s", bearing="%s"' % 
			(form.service_number_input.data, form.latitude.data, form.longitude.data, form.bearing.data))
		return render_template('services/input.html', form = form)