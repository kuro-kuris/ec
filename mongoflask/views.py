from flask import Blueprint, request, redirect, flash, render_template, url_for
from flask.views import MethodView

services = Blueprint('services', __name__, template_folder = 'templates')

class InputView(MethodView):

	@services.route('/', methods= ['GET', 'POST'])
	def img():
		return render_template('base.html')
