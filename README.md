Auto-Auth-Server v0.1
============================

A server and RESTful API service capable of authenticating through web portals for arbitrary websites and using machine learning techniques to determine login success / failure. Auto-Auth-Server is a Master’s project by Jay Waldron at Washington University in St. Louis. The work is covered under the Apache 2.0 license. This README deals specifically with setup instructions, but more information on the project itself, as well as a running demo server, may be found at https://www.auto-auth-server.com/

### Quick-Start:
To administer your own basic Auto-Auth-Server, only the portals.conf file must be modified to contain the info for the portals you’d like to authenticate with. A sample portals.conf file is provided, but look in the Important Files section of this README for how the file should be formatted. Otherwise, you can run the project like any other eclipse dynamic web project and can export it to a WAR file for more general use once your portals.conf has been configured. It’s been tested using Tomcat v7.0.

### Specialized Models:
Sometimes the general model can get things wrong, so it’s possible to generate specialized models used for individual portals. To do this, the database.properties file must point to a MySQL database setup for such use. A specialized model being created will search the database specified in database.properties for vectors matching the given portal_code in a table called custom_vectors. You do not need to create this yourself, and instead Custom Vector Tool will take care of all setup as long as the DB credentials have table creation permissions. See Custom Vector Tool (https://github.com/foxfire206/Auto-Auth-Server-Custom-Vector-Tool) for more info. A specialized model is a simple C4.5 decision tree that is built based entirely on cookies for the site. In general, you want to collect as many distinct feature vectors as possible using Custom Vector Tool to generate a more reliable model, but you MUST have at least one case of successful authentication and one case of unsuccessful authentication.


Important Files 
============================
Below are a list of configurable or otherwise relevant files & directories:

### /WebContent/WEB-INF/classes/portals.conf: 
This defines all the portals that initialize on startup and that are opened to API usage once they’ve finished loading. The file consists of a JSON object with only the “portals” key with its value as a JSON array object. Each comma-separated item within the JSON array is its own JSON object which defines an individual portal, instructing the server how to interact with its corresponding website. The required keys that must be defined for each portal are listed below:

#### portal_code: 
The portal label which is advertised to API users. Must be a lowercase, completely alphabetical string.
#### auth_url: 
The url to start on. This is often where the login form is located.
#### user_field_xpath: 
The XPath of the site’s textbox that accepts user id
#### pass_field_xpath: 
The XPath of the textbox that accepts password
#### submit_button_xpath: 
The XPath of the button/image that is clicked to submit the login info
#### max_attempts: 
Used internally by the server to limit the number of incorrect login attempts. If a username / portal combination tries login unsuccessfully this many times, they will receive an instant negative login response regardless of future attempts, and such future attempts will not be forwarded to the portal.
#### attempts_reset_time: 
The number of minutes after which incorrect login attempts are forgiven, and the counter (which is compared against max_attempts) is reset to 0.

#### In addition to these 7 required fields, there are 2 optional ones:

#### specialized_model: 
If specified, must be "true" or "false". If omitted or explicitly set to "false", the portal uses the generic pre-generated model to classify attempted login responses. If set to “true”, a specialized model is created for page classification using only samples generated for that portal. In order to use this, MySQL must be setup and page-specific features scraped using the Custom Vector Tool (see https://github.com/foxfire206/Auto-Auth-Server-Custom-Vector-Tool for more info).
#### first_click_xpath: 
Some pages that use AJAX to generate content on-click can obscure the login form until an initial element is clicked. If specified, the element with the given XPath is clicked first before attempting to find the other user, pass, and submit XPaths.

All values in the JSON object specifying a portal are string representations, and special characters must be escaped like normal in JSON. XPath values of a web element can easily be found in most browsers. In Chrome, right-click the element > Inspect Element > right-click the node > Copy XPath.  An example of a valid portals.conf file is shown below.

```json
{"portals":[
{
	"portal_code":"wustl",
	"auth_url":"https://connect.wustl.edu/loginpage",
	"user_field_xpath":"//*[@id=\"ucWUSTLKeyLogin_txtUsername\"]",
	"pass_field_xpath":"//*[@id=\"ucWUSTLKeyLogin_txtPassword\"]",
	"submit_button_xpath":"//*[@id=\"ucWUSTLKeyLogin_btnLogin\"]",
	"max_attempts":"5",
	"attempts_reset_time":"5",
}
,
{
	"portal_code":"anotherportal",
	"auth_url":"https://wherever.com",
	"user_field_xpath":"username_XPath",
	"pass_field_xpath":"password_XPath",
	"submit_button_xpath":"submit_XPath",
	"max_attempts":"10",
	"attempts_reset_time":"60",
  "specialized_model":"true",
  "first_click_xpath": "click_login_label_XPath"
}
]}
```

### /WebContent/WEB-INF/classes/database.properties:
Has the database connection information to store your specialized portal vectors if you are using specialized models. See the Custom Vector Tool project for more info (https://github.com/foxfire206/Auto-Auth-Server-Custom-Vector-Tool).

### /WebContent/WEB-INF/classes/data.arff:
The file used by WEKA to understand feature vector formatting. Do not edit.

### /WebContent/WEB-INF/classes/rf.model:
The pre-generated general model used to classify portal response pages. Do not edit.

### /data/:
Holds additional related files not directly tied to execution such as a python demo script and some other predefined portals.


Project Dependencies
============================
This project relies heavily on several other open source third-party libraries and services. Thanks to the libraries listed below for their contribution to open-source development and for making their infrastructure available. Note: all required dependencies are already packaged with this project. However, to use specialized models, MySQL needs to be configured externally.

* Apache Commons IO 2.4
* AlchemyAPI 0.8
* Cloning 1.8.5
* HtmlUnit 2.11
* JavaMail 1.5.1
* Jersey 1.18
* Joda-Time 2.3
* Json-smart 2.0
* MySQL Connector/J 5.1.29
* Weka 3.7.10

Outside of 3rd party dependencies, this project relies on Custom Vector Tool to generate feature vectors required to build specialized models. See https://github.com/foxfire206/Auto-Auth-Server-Custom-Vector-Tool


Areas for Improvement
============================
This project is an initial release and can always improve. Below are a few of the more immediate ways it might do so:

### More flexible login:
Currently Auto-Auth-Server can only handle straightforward, single-stage login. Authentication that occurs using more than just username & password textboxes, over multiple pages, or with captchas are all examples of how login flexibility can be improved.

### Broader site handling:
HtmlUnit is an awesome headless browser that is the backbone of this project, but it still has its shortcomings. Some sites on the web utilize technology such as Flash or more specialized JS libraries that just aren’t navigable by HtmlUnit at the time of this writing. As such, there are many sites out there that Auto-Auth-Server should be able to automate login for in theory but in reality cannot.

### Model improvement and further experimentation:
Not much research has been done in classifying web pages in regards to their authentication success / failure. Further, the current model was generated using only 43 distinct samples split evenly between success & failure. More research into this, a significantly larger corpus of training data, or different modeling techniques such as clustering which would enable ongoing learning after the server is live, are all worth looking into.

### JUnit test case expansion:
Currently, the code base has over 90% test coverage. This is great, but coverage numbers aren’t everything. This is a large project built to handle a lot of concurrent activity, and while fundamentally the literal code is covered by test cases, some of the more large-scale concurrent and more conceptual test scenarios are lacking.



