"""Topology for Task 1.4

Adding the 'topos' dict with a key/value pair to generate our newly defined
topology enables one to pass in '--topo=task14topo' from the command line.
"""

from mininet.topo import Topo

class Task14Topo( Topo ):
    "Topology for Task 1.4"

    def __init__( self ):
        "Create topology"

        # Initialize topology
        Topo.__init__( self )

        # Add switches
        s1 = self.addSwitch('s1')
        s2 = self.addSwitch('s2')
        s3 = self.addSwitch('s3')
        s4 = self.addSwitch('s4')

        # Add hosts
        for h in xrange(1,4):
            host = self.addHost('h1%d' % h, ip = "10.10.1.%d" % h)
            self.addLink(host, s1)
        for h in xrange(1,4):
            host = self.addHost('h2%d' % h, ip = "10.10.2.%d" % h)
            self.addLink(host, s2)
        for h in xrange(1,4):
            host = self.addHost('h4%d' % h, ip = "10.10.4.%d" % h)
            self.addLink(host, s4)

        # Add remaining links
        self.addLink(s1, s2)
        self.addLink(s2, s3)
        self.addLink(s3, s4)

topos = { 'task14topo': ( lambda: Task14Topo() ) }
