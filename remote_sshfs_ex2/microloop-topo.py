"""Micro-loop topology for Assignment 3.3

    h1    h2    h3
    |     |     |
    s1 -- s2 -- s3
      \        /
       s5 -- s4

Forward frames from h1 and h2 to h3 on shortest paths.
Link s4-s5 has weight 3, all other links have weight 1.

Link s2 -- s3 is broken at some time, update forwarding tables to reroute, again
along shortest paths.

Adding the 'topos' dict with a key/value pair to generate our newly defined
topology enables one to pass in '--topo=microloop' from the command line.
"""

from mininet.topo import Topo

class MicroloopTopo( Topo ):
    "Micro-loop topology"

    def __init__( self, **opts ):
        # Initialize topology
        super( MicroloopTopo, self ).__init__( **opts )

        # Add hosts
        h1 = self.addHost( 'h1', ip='10.0.0.1' )
        h2 = self.addHost( 'h2', ip='10.0.0.2' )
        h3 = self.addHost( 'h3', ip='10.0.0.3' )

        # Add switches
        s1 = self.addSwitch( 's1', dpid='0000000000000001' )
        s2 = self.addSwitch( 's2', dpid='0000000000000002' )
        s3 = self.addSwitch( 's3', dpid='0000000000000003' )
        s4 = self.addSwitch( 's4', dpid='0000000000000004' )
        s5 = self.addSwitch( 's5', dpid='0000000000000005' )

        # Add links
        self.addLink( s1, s2 )
        self.addLink( s2, s3 )
        self.addLink( s3, s4 )
        self.addLink( s4, s5 )
        self.addLink( s5, s1 )

        self.addLink( h1, s1 )
        self.addLink( h2, s2 )
        self.addLink( h3, s3 )

# Register topology
topos = { 'microloop': ( lambda: MicroloopTopo() ) }
