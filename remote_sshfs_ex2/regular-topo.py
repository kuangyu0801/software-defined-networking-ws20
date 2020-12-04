"""Regular topology for Assignment 3.1

        s2---s4---s6
       /  \ /  \ /  \
h1---s1    X    X    s8---h2
       \  / \  / \  /
        s3---s5---s7

Route 1: h1 -> s1 -> s3 -> s4 -> s7 -> s8 -> h2
Route 2: h1 -> s1 -> s2 -> s4 -> s6 -> s8 -> h2

Adding the 'topos' dict with a key/value pair to generate our newly defined
topology enables one to pass in '--topo=regular' from the command line.
"""

from mininet.topo import Topo

class RegularTopo( Topo ):
    "Regular topology"

    def __init__( self, **opts ):
        # Initialize topology
        super( RegularTopo, self ).__init__( **opts )

        # Add hosts
        h1 = self.addHost( 'h1', ip='10.0.0.1' )
        h2 = self.addHost( 'h2', ip='10.0.0.2' )

        # Add switches
        s1 = self.addSwitch( 's1', dpid='0000000000000001' )
        s2 = self.addSwitch( 's2', dpid='0000000000000002' )
        s3 = self.addSwitch( 's3', dpid='0000000000000003' )
        s4 = self.addSwitch( 's4', dpid='0000000000000004' )
        s5 = self.addSwitch( 's5', dpid='0000000000000005' )
        s6 = self.addSwitch( 's6', dpid='0000000000000006' )
        s7 = self.addSwitch( 's7', dpid='0000000000000007' )
        s8 = self.addSwitch( 's8', dpid='0000000000000008' )

        # Add links
        for n in [h1, s2, s3]:
            self.addLink( s1, n )
        for n in [s2, s3, s6, s7]:
            self.addLink( n, s4 )
            self.addLink( n, s5 )
        for n in [h2, s6, s7]:
            self.addLink( s8, n )

# Register topology
topos = { 'regular': ( lambda: RegularTopo() ) }
