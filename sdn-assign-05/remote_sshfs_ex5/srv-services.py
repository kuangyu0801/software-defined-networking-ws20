#!/usr/bin/python2

# Dependencies
from shutil import copyfileobj
from time import sleep

import sys
import BaseHTTPServer
import argparse

try:
    from cStringIO import StringIO
except ImportError:
    from StringIO import StringIO

def messageWeb():
    f = StringIO()
    f.write( "<html>Thanks for requesting this web page!</html>\n" )
    return f

class HTTPRequestHandler( BaseHTTPServer.BaseHTTPRequestHandler ):
    "Serve HTTP GET requests with simulated processing delay"
    server_version = 'LoadBalancingHTTP/0.1'
    def do_GET( self ):
        global delay
        sleep(delay)
        f = messageWeb()
        l = f.tell()
        f.seek( 0 )
        self.send_response( 200 )
        encoding = sys.getfilesystemencoding()
        self.send_header( "Content-type", "text/html; charset=%s" % encoding )
        self.send_header( "Content-Length", str(l) )
        self.end_headers()
        copyfileobj( f, self.wfile )
        f.close()

def messageSSH():
    f = StringIO()
    f.write( "Thanks for requesting this ssh service!\n" )
    return f

class SSHRequestHandler( BaseHTTPServer.BaseHTTPRequestHandler ):
    "Serve HTTP GET requests with simulated processing delay"
    server_version = 'LoadBalancingHTTP/0.1'
    def do_GET( self ):
        global delay
        sleep(delay)
        f = messageSSH()
        l = f.tell()
        f.seek( 0 )
        self.send_response( 200 )
        encoding = sys.getfilesystemencoding()
        self.send_header( "Content-type", "text/html; charset=%s" % encoding )
        self.send_header( "Content-Length", str(l) )
        self.end_headers()
        copyfileobj( f, self.wfile )
        f.close()

# Start a HTTPServer using the HTTPRequestHandler
if __name__ == '__main__':
    global delay
    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--server', default='')
    parser.add_argument('-p', '--port'  , type=int  , default=80)
    parser.add_argument('-d', '--delay' , type=float, default=0.0)
    args = parser.parse_args()
    server_address = ( args.server, args.port )
    delay = args.delay
    
    if args.port == 8080:
	    print 'Serving HTTP GET requests on %s:%d (with a %.1fs processing delay)' % ( args.server, args.port, args.delay )
	    httpd = BaseHTTPServer.HTTPServer( server_address, HTTPRequestHandler )
    elif args.port == 9090:
        print 'Serving SSH GET requests on %s:%d (with a %.1fs processing delay)' % ( args.server, args.port, args.delay )
        httpd = BaseHTTPServer.HTTPServer( server_address, SSHRequestHandler )
    else:
        print 'Serving HTTP GET requests on %s:%d (with a %.1fs processing delay)' % ( args.server, args.port, args.delay )
        httpd = BaseHTTPServer.HTTPServer( server_address, HTTPRequestHandler )
	
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print 'Bye bye'
    finally:
        httpd.shutdown()
