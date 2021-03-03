"""A small 'fat tree' topology.
Adding the 'topos' dict with a key/value pair to generate our newly defined
topology enables one to pass in '--topo=fattree' from the command line.
"""

from mininet.topo import Topo

class FatTreeTopo( Topo ):
    "A small 'fat tree' topology."

    def __init__( self, coreBw=None, edgeBw=None, **opts ):
        """Create the fat tree topology.
        Arguments:
        coreBw -- the bandwidth of links between switches
        edgeBW -- the bandwidth of links between edge switches and hosts
        """
        coreOpts = { 'bw' : coreBw } if coreBw else {}
        edgeOpts = { 'bw' : edgeBw } if edgeBw else {}

        # Initialize topology
        super( FatTreeTopo, self ).__init__( **opts )

        # Add hosts (ip='10.0.<pod>.<host>')
        h1 = self.addHost( 'h1', ip='10.0.1.1' )
        h2 = self.addHost( 'h2', ip='10.0.1.2' )
        h3 = self.addHost( 'h3', ip='10.0.1.3' )
        h4 = self.addHost( 'h4', ip='10.0.1.4' )
        h5 = self.addHost( 'h5', ip='10.0.2.1' )
        h6 = self.addHost( 'h6', ip='10.0.2.2' )
        h7 = self.addHost( 'h7', ip='10.0.2.3' )
        h8 = self.addHost( 'h8', ip='10.0.2.4' )
        # Add core switches
        s1 = self.addSwitch( 's1', dpid='0000000000000001' )
        s2 = self.addSwitch( 's2', dpid='0000000000000002' )
        # Add pod switches (dpid='::<pod>:<switch>')
        # Pod 1
        s11 = self.addSwitch( 's11', dpid='0000000000000101' )
        s12 = self.addSwitch( 's12', dpid='0000000000000102' )
        s13 = self.addSwitch( 's13', dpid='0000000000000103' )
        s14 = self.addSwitch( 's14', dpid='0000000000000104' )
        # Pod 2
        s21 = self.addSwitch( 's21', dpid='0000000000000201' )
        s22 = self.addSwitch( 's22', dpid='0000000000000202' )
        s23 = self.addSwitch( 's23', dpid='0000000000000203' )
        s24 = self.addSwitch( 's24', dpid='0000000000000204' )

        # Add switch links
        for s in [s24, s23, s14, s13]:# s13, s14, s23, s24]:
            self.addLink( s, s1, **coreOpts )
            self.addLink( s, s2, **coreOpts )
        for s in [s13, s14]:
            self.addLink( s, s11, **coreOpts )
            self.addLink( s, s12, **coreOpts )
        for s in [s23, s24]:
            self.addLink( s, s21, **coreOpts )
            self.addLink( s, s22, **coreOpts )

        # Connect hosts
        self.addLink( h1, s11, **edgeOpts )
        self.addLink( h2, s11, **edgeOpts )
        self.addLink( h3, s12, **edgeOpts )
        self.addLink( h4, s12, **edgeOpts )
        self.addLink( h5, s21, **edgeOpts )
        self.addLink( h6, s21, **edgeOpts )
        self.addLink( h7, s22, **edgeOpts )
        self.addLink( h8, s22, **edgeOpts )

# Register topology
topos = { 'fattree': ( lambda: FatTreeTopo() ) }
