#!/usr/bin/python2

"""Pyretic Topology

         inet
          |
mon ---  s1  --- srv
        /  \
       h1  h2

"""

# Dependencies
#from mininet.topo import Topo
from mininet.net  import Mininet
from mininet.cli  import CLI
from mininet.node import OVSSwitch, RemoteController
from mininet.log  import setLogLevel, info
import os

class SRVCLI( CLI ):
    "Custom CLI with 'startservers' command to start HTTP listener processes."

    def __init__( self, mininet ):
        self.mininet = mininet
        CLI.__init__( self, mininet )

    def do_startservers( self, _line ):
        if self.mininet.httpd:
            info( '[servers already running]\n' )
            return
        
        d = '0.5'
        
        info( '*** Starting HTTP and SSH services on srv\n' )
        h = self.mininet.servers[0]
        self.mininet.httpd[h.name + '-http'] = h.popen( 'python2', os.path.dirname(os.path.realpath(__file__))+'/srv-services.py', '-s', h.IP(), '-p 8080', '-d', d )
        self.mininet.httpd[h.name + '-ssh']  = h.popen( 'python2', os.path.dirname(os.path.realpath(__file__))+'/srv-services.py', '-s', h.IP(), '-p 9090', '-d', d )
        
        info( '*** Starting HTTP service on inet\n' )
        h = self.mininet.servers[1]
        self.mininet.httpd[h.name + '-http'] = h.popen( 'python2', os.path.dirname(os.path.realpath(__file__))+'/srv-services.py', '-s', h.IP(), '-p 8080', '-d', d )
        
        info( '\n' )

class SRVMininet( Mininet ):
    "Custom Mininet for pyretic task"

    def __init__( self ):
        Mininet.__init__( self, switch=OVSSwitch, controller=RemoteController )
        self.servers = []   # list of server nodes
        self.httpd = {}     # dict of HTTP listener processes

    def build( self ):
        # Add remote controllers
        c0 = self.addController( 'c0' )
        
        # Add controller-bound switches
        s1 = self.addSwitch( 's1', dpid='0000000000000001' )
        
        # Add client node
        self.h1   = self.addHost( 'h1'  , ip='10.0.0.1', mac='000000000001' )
        self.h2   = self.addHost( 'h2'  , ip='10.0.0.2', mac='000000000002' )
        self.mon  = self.addHost( 'mon' , ip='10.0.0.3', mac='000000000003' )
        
        # Add server nodes with private IP addresses
        self.servers.append( self.addHost( 'srv' , ip='10.0.0.4', mac='000000000101' ) )
        self.servers.append( self.addHost( 'inet', ip='10.0.0.5', mac='000000000102' ) )
        
        # Add links
        self.addLink( self.h1        , s1 )
        self.addLink( self.h2        , s1 )
        self.addLink( self.mon       , s1 )
        self.addLink( self.servers[0], s1 ) # srv
        self.addLink( self.servers[1], s1 ) # inet
        
        # Build Mininet
        Mininet.build( self )

    def start( self ):
        info( '=== Starting Mininet ===\n' )
        Mininet.start( self )
        
        # ARP
        self.h1.setARP( self.h2.IP(), self.h2.MAC() )
        self.h1.setARP( self.servers[0].IP(), self.servers[0].MAC() )
        self.h1.setARP( self.servers[1].IP(), self.servers[1].MAC() )
        
        self.h2.setARP( self.h1.IP(), self.h1.MAC() )
        self.h2.setARP( self.servers[0].IP(), self.servers[0].MAC() )
        self.h2.setARP( self.servers[1].IP(), self.servers[1].MAC() )
        
        self.mon.setARP( self.servers[0].IP(), self.servers[0].MAC() )
        self.mon.setARP( self.servers[1].IP(), self.servers[1].MAC() )
        
        self.servers[1].setARP( self.servers[0].IP(), self.servers[0].MAC() )
        self.servers[0].setARP( self.servers[1].IP(), self.servers[1].MAC() )
        
        for h in self.servers:
            h.cmd( 'route add -net 10.0.0.0 netmask 255.255.255.0 dev %s-eth0' % h.name )
        
        h = self.mon
        h.cmd( 'route add -net 10.0.0.0 netmask 255.255.255.0 dev %s-eth0' % h.name )

    def stop( self ):
        if len(self.httpd) > 0:
            info( '*** Stopping %d HTTP servers\n' % len(self.httpd) )
            for hostname, process in self.httpd.iteritems():
                # Stop HTTP server instance
                info( hostname + ' ' )
                process.terminate()
                process.wait()
            self.httpd = {}
            info( '\n' )
        Mininet.stop( self )

if __name__ == '__main__':
    # Enable logging to the terminal
    setLogLevel( 'info' )
    
    # Create and start SRVMininet
    net = SRVMininet()
    net.start()
    
    # Start CLI
    SRVCLI( net )
    
    # When user exits CLI, stop Mininet
    net.stop()



