#!/usr/bin/python

from mininet.net import Mininet
from mininet.cli import CLI
from mininet.node import OVSSwitch, RemoteController
from mininet.link import TCLink
from mininet.log import setLogLevel, info, output, error
from mininet.util import pmonitor
from fattree import FatTreeTopo
from sys import exit
from time import sleep
import re

class LoadtestCLI( CLI ):
    "Extension of mininet.cli.CLI with loadtest."
    def __init__( self, mininet ):
        self.hostmap = {}
        for host in mininet.hosts:
            self.hostmap[ host.name ] = host
        CLI.__init__( self, mininet )

    def runLoadtest( self, sHost=None, cHost=None, nIperfs=4 ):
        if not sHost:
            sHost = sorted(self.hostmap.items())[0][1]
        if not cHost:
            cHost = sorted(self.hostmap.items())[-1][1]
        dt = 5
        ct = 20
        output( "*** Throughput test between %s and %s\n" % ( sHost.name, cHost.name ) )
        popens = {}
        # Start iperf server
        server = sHost.popen( "iperf -f k -s -p 5001, -P %d" % nIperfs )
        popens['server'] = server
        output( "  * This should take at least %d seconds (possibly much longer!)\n" % ( dt*nIperfs+ct ) )
        # Start iperf clients
        output( "    - Starting flows [%s]" % (" "*nIperfs) )
        for i in xrange(1,nIperfs+1):
            output( "%s=%s]" % ( "\b"*(nIperfs-i+2) , " "*(nIperfs-i)  ) )
            popens[ str(i) ] = cHost.popen( "iperf -f k -c %s -p 5001 -t %d" % ( sHost.IP(), dt*(nIperfs-i)+ct ) )
            if i < nIperfs:
                sleep(dt)
        output( "\n    - Waiting for flows to terminate...\n" )
        # Monitor output
        cout = {}
        sout = ''
        for n, line in pmonitor( popens ):
            if n:
                if n == 'server':
                    sout = line.strip()
                else:
                    cout[n] = line.strip()

        output( "  * Format:  [ ID] Interval       Transfer     Bandwidth\n" )
        for c in sorted(cout.keys()):
            output( "  * Flow %2s: %s\n" % ( c, cout[c] ) )
        output( "*** Summary: %s\n" % sout )

    def do_loadtest( self, line ):
        '''Throughput test between two hosts using multiple iperfs.

        Usage: loadtest [<host1> <host2> [<n>]]
          <host1> iperf server      (default: first host in network)
          <host2> iperf client       (default: last host in network)
              <n> number of simultaneous iperfs         (default: 4)'''
        args = line.split()
        if len( args ) not in [0,2,3]:
            error( 'Invalid number of args: loadtest [<host1> <host2> [<n>]]\n' )
        elif len( args ) == 0:
            self.runLoadtest()
        else: # len( args ) in [2,3]
            hosts = []
            hostfault = False
            for arg in args[0:2]:
                if arg not in self.hostmap:
                    hostfault = True
                    error( "'%s' is not a valid host in the network\n" % arg )
                else:
                    hosts.append( self.hostmap[ arg ] )
            if hostfault:
                error( 'Please enter valid hosts\n' )
            elif len( args ) == 2:
                self.runLoadtest( hosts[0], hosts[1] )
            else: # len( args ) == 3
                self.runLoadtest( hosts[0], hosts[1], int(args[2]) )


def startNet():
    # Initialize Mininet
    fattree = FatTreeTopo( coreBw=2, edgeBw=10 )
    net = Mininet( build=False, link=TCLink, switch=OVSSwitch, controller=RemoteController, topo=fattree, autoStaticArp=True )
    net.addController( 'c0', port=6653 ) # use Floodlight's default OF port
    net.build()

    # Start Mininet
    info( '=== Starting Mininet ===\n' )
    net.start()

    # Start CLI
    try:
        LoadtestCLI( net )
    except KeyboardInterrupt:
        # catch KeyboardInterrupt during execution of long commands in CLI
        net.stop()
        exit(0)

    # Stop Mininet
    info( '=== Stopping Mininet ===\n' )
    net.stop()


if __name__ == '__main__':
    setLogLevel( 'info' )
    startNet()
