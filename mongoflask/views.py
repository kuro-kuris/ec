from flask import Blueprint, request, redirect, render_template, url_for
from flask.views import MethodView
from mongoflask.models import Service, Stop

services = Blueprint('services', __name__, template_folder = 'templates')

class ListView(MethodView):

	def get(self):
		services = Service.objects.all()
		return render_template('services/list.html', services = services)

class DetailView(MethodView):

	def get(self, name):
		service = Service.objects.get_or_404(name = name)
		return render_template('services/detail.html', service = service)



# Register the urls

services.add_url_rule('/', view_func = ListView.as_view('list'))
services.add_url_rule('/<name>/', view_func = DetailView.as_view('detail'))