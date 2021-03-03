"""Monitoring topology for Assignment 3.2

         h2
         |
         s1
         |
  src -- s2 -- s3 -- sink
               |
               s4
               |
               h1

'src' - periodically sends data to 'sink' (IP 10.0.0.10)
h1 and h2 - transparently log the (unicast) traffic from 'src' to 'sink'

Any frame from 'src' must be forwarded to 'sink'
and either 'h1' OR 'h2', but not both (logging consistency)!

Adding the 'topos' dict with a key/value pair to srcerate our newly defined
topology enables one to pass in '--topo=monitors' from the command line.
"""

from mininet.topo import Topo

class MonitorsTopo( Topo ):
    "Monitoring topology"

    def __init__( self, **opts ):
        # Initialize topology
        super( MonitorsTopo, self ).__init__( **opts )

        # Add hosts
        src = self.addHost( 'src', ip='10.0.0.1/24' )
        sink = self.addHost( 'sink', ip='10.0.0.10/24' )
        h1 = self.addHost( 'h1', ip='192.168.1.1/24' )
        h2 = self.addHost( 'h2', ip='192.168.1.2/24' )

        # Add switches
        s1 = self.addSwitch( 's1', dpid='0000000000000001' )
        s2 = self.addSwitch( 's2', dpid='0000000000000002' )
        s3 = self.addSwitch( 's3', dpid='0000000000000003' )
        s4 = self.addSwitch( 's4', dpid='0000000000000004' )

        # Add links
        self.addLink( src, s2 )
        self.addLink( s2, s3 )
        self.addLink( s3, sink )
        self.addLink( h2, s1 )
        self.addLink( s1, s2 )
        self.addLink( h1, s4 )
        self.addLink( s4, s3 )

# Register topology
topos = { 'monitors': ( lambda: MonitorsTopo() ) }
