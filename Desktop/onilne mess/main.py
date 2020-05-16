import os
import bcrypt
from datetime import date, datetime, timedelta
import random
from string import ascii_lowercase, digits

from flask import Flask
from flask import render_template, redirect, url_for, request
from flask import session, flash, send_from_directory
from flask_mail import Mail
from flask_sqlalchemy import SQLAlchemy

from werkzeug.utils import secure_filename

app = Flask(__name__)
app.secret_key = "Hello"

IMAGE_EXTENSIONS = ["png", "jpg", "jpeg", "bmp"]
SERVER_URL = "http://127.0.0.1:5000/"

# Database Section
app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///users.sqlite3"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
app.config["UPLOAD_FOLDER"] = "res/images"
app.permanent_session_lifetime = timedelta(minutes=5)
db = SQLAlchemy(app)


class UsersDB(db.Model):
	id = db.Column(db.Integer, primary_key=True)
	first_name = db.Column(db.String(20), nullable=False)
	last_name = db.Column(db.String(20), nullable=False)
	password = db.Column(db.String(128), unique=True)
	email = db.Column(db.String(80))
	account_type = db.Column(db.String(80))
	verified = db.Column(db.Boolean)
	restaurant = db.Column(db.Integer)
	subscriptions = db.Column(db.String(300))

class RestaurantsDB(db.Model):
	id = db.Column(db.Integer, primary_key=True, unique=True)
	name = db.Column(db.String(80))
	menu = db.Column(db.String(80))
	address = db.Column(db.String(80))
	phone = db.Column(db.String(20))
	website = db.Column(db.String(80))
	timing = db.Column(db.String(80))
	tags = db.Column(db.String(80))
	owner = db.Column(db.Integer)
	images = db.relationship('ImagesDB', backref='parent_rest')
	monthly_items = db.Column(db.String(300))
	rating = db.Column(db.Integer)
	vegetarian = db.Column(db.Boolean)
	average_cost = db.Column(db.Float)

class ImagesDB(db.Model):
	id = db.Column(db.Integer, primary_key=True, unique=True)
	image_name = db.Column(db.String(40))
	parent_id = db.Column(db.Integer, db.ForeignKey("restaurantsDB.id"))

db.create_all()
# Database End

# Email sending
SENDER_EMAIL = "marathi.naukri.app@gmail.com"
SENDER_PASS = "asdzxcqwe123)(*"

app.config.update(
	MAIL_SERVER = 'smtp.gmail.com',
	MAIL_PORT = '465',
	MAIL_USE_SSL = True,
	MAIL_USERNAME = SENDER_EMAIL,
	MAIL_PASSWORD = SENDER_PASS
)
mail = Mail(app)

def send_email(recipient, subject, body):
	with app.app_context():
		mail.send_message(subject,
							sender=SENDER_EMAIL,
							recipients = [recipient],
							body = body)

reset_keys = {}
verification_codes = {}

def check_details(*details):
	for detail in details:
		if not detail:
			return False
	return True

@app.route("/")
@app.route("/home")
def home():
	# db.drop_all()
	# db.session.commit()
	#db.session.query(RestaurantsDB).delete()
	#currentUser = UsersDB.query.filter_by(username=session["user"]).first()
	if "user" in session:
		loggedIn = [session["username"], session["account_type"]]
	else:
		loggedIn = None
	return render_template("home.html", logged_in=loggedIn)

@app.route("/search", methods=["GET"])
def search():
	query_result = []
	if request.method == "GET":
		text_query = request.args.get("text_query_name")
		if text_query != None:
			#query_result = RestaurantsDB.query.filter(RestaurantsDB.name.like(f"%{text_query_name}%")).all()
			text_query_list = text_query.split()
			query_result = RestaurantsDB.query.filter(RestaurantsDB.name.contains(text_query)).all()
			for restaurant in RestaurantsDB.query.all():
				if restaurant in query_result:
					continue
				for text_query_item in text_query_list:
					if restaurant in query_result:
						continue
					for sub_address in restaurant.address.split(", "):
						if text_query_item in sub_address.split():
							query_result.append(restaurant)
							break
				for text_query_item in text_query_list:
					if restaurant in query_result:
						continue
					for tag in restaurant.tags.split(", "):
						if text_query_item in tag:
							query_result.append(restaurant)
							break
		else:
			text_query = ""

		#query_result = RestaurantsDB.query.filter(RestaurantsDB.name.in_(text_query.split())).all()
	if "user" in session:
		loggedIn = [session["username"], session["account_type"]]
	else:
		loggedIn = None
	return render_template("search.html", logged_in=loggedIn, query_result=query_result, text_query=text_query)

@app.route("/login", methods=["GET", 'POST'])
def login():
	if request.method == "POST":
		text_email = request.form["text_email"]
		text_password = request.form["text_password"]
		if not check_details(text_email, text_password):
			flash("Please fill all the details", "error")
			return render_template("home.html", logged_in=None)

		currentUser = UsersDB.query.filter_by(email=text_email).first()
		if currentUser:
			if not bcrypt.checkpw(text_password.encode(), currentUser.password):
				flash("Incorrect password or email!!", "error")
				return render_template("home.html", logged_in=None, pop_up="login") 
		else:
			flash("No such user, consider signing up", "error")
			return render_template("home.html", logged_in=None, pop_up="login")
			
		currentUser = UsersDB.query.filter_by(email=text_email).first()
		session["user"] = currentUser.email
		session["username"] = f"{currentUser.first_name} {currentUser.last_name}"
		session["account_type"] = currentUser.account_type
		session.permanent = True
		loggedIn = [session["username"], session["account_type"]]

#	if "user" in session:
	return redirect(url_for("home"))
#	else:
#		return render_template("home.html", logged_in=loggedIn, pop_up=None)

@app.route("/signup", methods=["GET", "POST"])
def signup():
	if request.method == "POST":
		text_first_name = request.form["text_first_name"]
		text_last_name = request.form["text_last_name"]
		text_email = request.form["text_email"]
		text_password = request.form["text_password"]
		# flash("".join([*request.form]), "error")
		radio_account_type = request.form["radio_account_type"]
		if not check_details(text_first_name, text_last_name, text_email, text_password, radio_account_type):
			flash("Please fill all the details", "error")
			return render_template("home.html", logged_in=None, pop_up="signup")
		if not text_password == request.form["text_repeat_password"]:
			flash("Passwords do not match", "error")
			return render_template("home.html", logged_in=None, pop_up="signup")
		
		currentUser = UsersDB.query.filter_by(email=text_email).first()
		if currentUser:
			flash("Email already registered", "error")
			return render_template("home.html", logged_in=None, pop_up=None)

		db.session.add(UsersDB(first_name=text_first_name, last_name=text_last_name, email=text_email, password=bcrypt.hashpw(text_password.encode(), bcrypt.gensalt()), account_type=radio_account_type, verified=0b0, restaurant=-1))
		db.session.commit()

		currentUser = UsersDB.query.filter_by(email=text_email).first()
		session["user"] = currentUser.email
		session["username"] = f"{currentUser.first_name} {currentUser.last_name}"
		session["account_type"] = currentUser.account_type
		session.permanent = True
		loggedIn = [session["username"], session["account_type"]]

#	if "user" in session:
	return redirect(url_for("home"))
#	else:
#		return render_template("home.html", logged_in=[session["username"], session["account_type"]], pop_up=None)

@app.route("/forgot", methods=["POST", "GET"])
def forgot():
	global reset_keys
	allow_key = False
	text_email = ""
	label_text = "Reset password"
	if request.method == "POST":
		text_email = request.form["text_email"]
		if text_email:
			currentUser = UsersDB.query.filter_by(email=text_email).first()
			if currentUser:
				if "text_password_key" in request.form:
					text_password_key = request.form["text_password_key"]
					text_new_password = request.form["text_new_password"]
					if check_details(text_password_key, text_new_password):
						if bcrypt.checkpw(text_password_key.encode(), reset_keys[currentUser.email]):
							currentUser.password = bcrypt.hashpw(text_new_password.encode(), bcrypt.gensalt())
							db.session.commit()
							del reset_keys[currentUser.email]
							flash("Password changed", "success")
						else:
							flash("Invalid details", "error")
							allow_key = True
					else:
						flash("Please fill all the details")
						allow_key = True
					
				else:
					if currentUser.email in reset_keys:
						flash("Email has already been sent")
					else:
						reset_key = "".join([random.choice(ascii_lowercase + digits) for letter in range(8)])
						reset_keys[currentUser.email] = bcrypt.hashpw(reset_key.encode(), bcrypt.gensalt())
						send_email("riteshdthakur@gmail.com", "Password change requested", f"A password change request has been submitted for your account\n <b>Your password reset key is {reset_key}</b>\n If you did not request for the same, please ignore this email")
						flash("Email has been sent", "success")
					allow_key = True
			else:
				flash("Invalid email", "error")
	if "user" in session:
		text_email = session["user"]
		label_text = "Change your password"
		loggedIn = [session["username"], session["account_type"]]
	else:
		loggedIn = None

	return render_template("forgot.html", logged_in=loggedIn, allow_key=allow_key, email=text_email, label_text=label_text)

@app.route("/logout")
def logout():
	if "user" in session:
		session.pop("user", None)
		session.pop("email", None)
		session.pop("username", None)
		session.pop("account_type", None)
		flash("Successfully logged out!!", "success")
	loggedIn = None
	return redirect(url_for("home", logged_in=loggedIn))

@app.route("/verify/<verification_code>")
def verify(verification_code):
	global verification_codes
	if verification_code in verification_codes:
		currentUser = UsersDB.query.filter_by(email=verification_codes[verification_code]).first()
		if currentUser:
			currentUser.verified = True
			db.session.commit()
			print("Verified")
		del verification_codes[verification_code]
	return redirect(url_for("home"))

@app.route("/user", methods=["POST", "GET"])
def user():
	pass

@app.route("/upload/<filename>")
def send_image(filename):
	return send_from_directory(app.config["UPLOAD_FOLDER"], filename)

@app.route("/admin", methods=["POST", "GET"])
def admin():
	if request.method == "POST":
		text_name = request.form["text_name"]
		text_cuisine = request.form["text_cuisine"]
		text_menu = request.form["text_menu"]
		text_address = request.form["text_address"]
		text_phone = request.form["text_phone"]
		text_website = request.form["text_website"]
		text_timing = request.form["text_timing"]
		text_tags = request.form["text_tags"]

		db.session.add(RestaurantsDB(name=text_name, cuisine=text_cuisine, menu=text_menu, address=text_address, phone=text_phone, website=text_website, timing=text_timing, tags=text_tags))
		db.session.commit()
	return render_template("admin.html", restaurant_list=RestaurantsDB.query.all(), logged_in=[session["username"], session["account_type"]], username=session["user"])

@app.route("/restaurant/<restaurant_id>", methods=["POST", "GET"])
def restaurant(restaurant_id):
	if "user" in session:
		loggedIn = [session["username"], session["account_type"]]
	else:
		loggedIn = None
	currentRest = RestaurantsDB.query.filter_by(id=restaurant_id).first()
	if currentRest:
		if request.method == "POST":
			if "user" in session:
				currentUser = UsersDB.query.filter_by(email=session["user"]).first()
				if "button_subscribe_" in str(request.form):
					button_subscribe = list(filter(lambda x: "button_subscribe_" in x, request.form.keys()))[0].split("_")[-1]
					print(currentUser.subscriptions)
					if currentUser.subscriptions != None:
						currentUser.subscriptions += f", {currentRest.id}|{button_subscribe}|{datetime.now()+timedelta(days=30)}"
					else:
						currentUser.subscriptions = f"{currentRest.id}|{button_subscribe}|{datetime.now()+timedelta(days=30)}"
					flash("Subscription added!!", "success")
					db.session.commit()
			else:
				flash("Please login first!!", "error")
		return render_template("restaurant.html", restaurant=currentRest, logged_in=loggedIn)
	else:
		return render_template("search.html", logged_in=loggedIn)

@app.route("/user/subscriptions", methods=["POST", "GET"])
def subscriptions():
	global verification_codes
	if not "user" in session:
		return redirect(url_for("logout"))
	else:
		loggedIn = [session["username"], session["account_type"]]
	currentUser = UsersDB.query.filter_by(email=session["user"]).first()

	if not currentUser:
		flash("Please login again!!", "error")
		return redirect(url_for("logout"))
	if request.method == "POST":
		if "button_verify" in request.form:
			verification_code = "".join([random.choice(ascii_lowercase + digits) for letter in range(16)])
			verification_codes[verification_code] = currentUser.email
			send_email("riteshdthakur@gmail.com", "Email verification", f"Please verify this email for your account\n{SERVER_URL}/verify/{verification_code}")
			flash("Verification email has been sent", "success")


	subscribed_list = []
	updated_subscriptions = currentUser.subscriptions.split(", ")
	for subscription in currentUser.subscriptions.split(", "):
		if subscription:
			restaurant_id = int(subscription.split("|")[0])
			item = int(subscription.split("|")[1])
			end_date = datetime.strptime(subscription.split("|")[2], '%Y-%m-%d %H:%M:%S.%f')

			restaurant = RestaurantsDB.query.filter_by(id=restaurant_id).first()
			if restaurant:
				item_name, item_price = restaurant.menu.split("| ")[item].split("~")[:2]
				if datetime.now() > end_date:
					updated_subscriptions.remove(subscription)
					flash(f"{restaurant.name} - Subscription expired on {end_date.strftime('%a, %d %b %Y')}", "error")
					continue
				subscribed_list.append([restaurant, item_name, int(item_price)*30*0.9, end_date.strftime("%a, %d %b %Y")])
	currentUser.subscriptions = ", ".join(updated_subscriptions)
	db.session.commit()
	return render_template("subscriptions.html", logged_in=loggedIn, email=session["user"], subscribed_list=subscribed_list)

@app.route("/user/restaurant", methods=["POST", "GET"])
def user_restaurant():
	if not "user" in session:
		return redirect(url_for("logout"))
	else:
		loggedIn = [session["username"], session["account_type"]]
	currentUser = UsersDB.query.filter_by(email=session["user"]).first()

	if not currentUser:
		flash("Please login again!!", "error")
		return redirect(url_for("logout"))
	if currentUser.account_type == "Regular":
		flash("Permission denied (You don't have a business account)!!", "error")
		return redirect(url_for("subscriptions", logged_in=[session["username"], session["account_type"]]))
	currentRest = RestaurantsDB.query.filter_by(owner=currentUser.id).first()
	if not currentRest:
		currentRest = {key: "" for key in ["name", "address", "phone", "website", "timing", "tags", "rating"]}
		list_menu = [("", "") for i in range(15)]
	else:
		list_menu = [tuple(item.split("~")) for item in currentRest.menu.split("| ")]

	if request.method == "POST":
		print(request.form)
		if "file_image" in request.files:
			if "button_image_delete_" in str(request.form):
				for key in request.form:
					if "button_image_delete_" in key:
						image_id = int(key.split("_")[-1])
						imageObject = ImagesDB.query.filter_by(id=image_id).first()
						os.remove(os.path.join(app.config["UPLOAD_FOLDER"], f"{imageObject.image_name}"))
						db.session.delete(imageObject)
						db.session.commit()
			else:
				file_data = request.files["file_image"]
				if file_data.filename ==  "":
					flash("No file selected", "error")
				if file_data: # Image check
					next_filename = str(max([int(name.split(".")[0]) for name in os.listdir(app.config["UPLOAD_FOLDER"]) if name.split(".")[-1] in IMAGE_EXTENSIONS]+[0])+1)
					filename = secure_filename(next_filename + "." + file_data.filename.split(".")[-1])
					file_path = os.path.join(app.config["UPLOAD_FOLDER"], filename)
					file_data.save(file_path)
					db.session.add(ImagesDB(image_name=filename, parent_rest=currentRest))
					db.session.commit()
		elif "text_name" in request.form:
			text_name = request.form["text_name"]
			if "checkbox_vegetarian" in request.form:
				checkbox_vegetarian = 0b1
			else:
				checkbox_vegetarian = 0b0
			text_address = request.form["text_address"]
			text_phone = request.form["text_phone"]
			text_website = request.form["text_website"]
			text_timing = request.form["text_timing"]
			text_hygiene_rating = int(request.form["text_hygiene_rating"])
			text_tags = request.form["text_tags"]
			if checkbox_vegetarian == 0b1 and "vegan" not in text_tags:
				text_tags += ", vegan"
			text_menu_names = [request.form[f"text_menu_name_{i}"] for i in range(15) if f"text_menu_name_{i}" in request.form]
			text_menu_prices = [request.form[f"text_menu_price_{i}"] for i in range(15) if f"text_menu_price_{i}" in request.form]
			average_cost = sum(list(map(int, [item for item in text_menu_prices if item != ""])))/len([item for item in text_menu_names if item != ""])

			text_menu_subscribe = [0 for _ in range(15)] 
			for key in filter(lambda x: "checkbox_menu_subscribe_" in x, request.form.keys()):
				text_menu_subscribe[int(key.split("_")[-1])] = 1
			
			if not check_details(text_name, text_address, text_phone, text_website, text_timing, text_tags, text_menu_names, text_menu_prices):
				flash("Please fill all the restaurant details", "error")
			else:

				text_menu = "| ".join([f"{name}~{price}~{subscribe}" for name, price, subscribe in zip(text_menu_names, text_menu_prices, text_menu_subscribe)])

				if not RestaurantsDB.query.filter_by(owner=currentUser.id).first():
					flash("Added your restaurant successfully!!!", "success")
					db.session.add(RestaurantsDB(name=text_name, address=text_address, phone=text_phone, website=text_website, timing=text_timing, tags=text_tags, menu=text_menu, owner=currentUser.id, rating=text_hygiene_rating, vegetarian=checkbox_vegetarian, average_cost=average_cost))
					currentRest = RestaurantsDB.query.filter_by(owner=currentUser.id).first()
				else:
					flash("Updated your restaurant details successfully!!!", "success")
					currentRest.name = text_name
					currentRest.address=text_address
					currentRest.phone=text_phone
					currentRest.website=text_website
					currentRest.timing=text_timing
					currentRest.tags=text_tags
					currentRest.menu=text_menu
					currentRest.owner=currentUser.id
					currentRest.rating = text_hygiene_rating
					currentRest.vegetarian = checkbox_vegetarian
					currentRest.average_cost = average_cost
				db.session.commit()
			
				list_menu = [tuple(item.split("~")) for item in currentRest.menu.split("| ")]

	return render_template("user_restaurant.html", logged_in=loggedIn, account_type=session["account_type"], restaurant=currentRest, menu=list_menu)

if __name__ == "__main__":
	app.run(debug=True)