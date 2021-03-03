"""Topology for Task 1.2

Two directly connected switches with two hosts each:

          s1 ---- s2
         /  \    /  \
        h1  h2  h3  h4

Adding the 'topos' dict with a key/value pair to generate our newly defined
topology enables one to pass in '--topo=task12topo' from the command line.
"""

from mininet.topo import Topo

class Task12Topo( Topo ):
    "Topology for Task 1.2"

    def __init__( self ):
        "Create topology"

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        h1 = self.addHost( 'h1' )
        h2 = self.addHost( 'h2' )
        h3 = self.addHost( 'h3' )
        h4 = self.addHost( 'h4' )

        s1 = self.addSwitch( 's1' )
        s2 = self.addSwitch( 's2' )

        # Add links
        # Hint: definition order defines switchport-number assignment
        self.addLink( s1, s2 )

        self.addLink( h1, s1 )
        self.addLink( h2, s1 )

        self.addLink( h4, s2 )
        self.addLink( h3, s2 )


topos = { 'task12topo': ( lambda: Task12Topo() ) }
