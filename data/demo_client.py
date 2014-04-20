import urllib2, urllib, sys, getpass

if len(sys.argv) != 2:
	print '1 argument required: the url where the auto-auth-server login api resides.\n\tExample: https://www.auto-auth-server.com/api/login'
	sys.exit(0)

portal_code = raw_input('portal_code: ')
username = raw_input('username: ')
password = getpass.getpass('password: ')

print 'working...\n'
mydata=[('password',password),('username',username),('portal_code',portal_code)] 
mydata=urllib.urlencode(mydata)
req=urllib2.Request(sys.argv[1], mydata)
req.add_header("Content-type", "application/x-www-form-urlencoded")
page=urllib2.urlopen(req).read()

print 'response from server: '
print page
print ''