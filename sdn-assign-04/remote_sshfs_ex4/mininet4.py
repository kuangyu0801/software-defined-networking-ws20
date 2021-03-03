#!/usr/bin/env python2

from mininet.net import Mininet
from mininet.cli import CLI
from mininet.log import setLogLevel, info, output, error
from mininet.node import OVSSwitch, Node, RemoteController
from mininet.link import Link

if __name__ == '__main__':
    setLogLevel( 'info' )

    # Initialize Mininet
    net = Mininet( switch=OVSSwitch, controller=RemoteController, autoSetMacs=True, build=False )

    c = net.addController( 'c', port=6653 )

    s1 = net.addSwitch( 's1', dpid='00:00:00:00:00:00:00:01' )
    s2 = net.addSwitch( 's2', dpid='00:00:00:00:00:00:00:02' )
    s3 = net.addSwitch( 's3', dpid='00:00:00:00:00:00:00:03' )
    net.addLink( s1, s2 )
    net.addLink( s2, s3 )

    pub1 = net.addHost( 'pub1', ip='10.1.0.1/8' )
    sub1 = net.addHost( 'sub1', ip='10.1.1.1/8' )
    sub2 = net.addHost( 'sub2', ip='10.1.1.2/8' )
    sub3 = net.addHost( 'sub3', ip='10.1.1.3/8' )
    sub4 = net.addHost( 'sub4', ip='10.1.1.4/8' )
    net.addLink( pub1, s1 )
    net.addLink( sub1, s2 )
    net.addLink( sub2, s3 )
    net.addLink( sub3, s2 )
    net.addLink( sub4, s2 )

    # Connect to root namespace (borrowed from mininet/examples/sshd.py)
    # Add interface to s1
    info( '*** Adding gateway 10.10.10.10 to root namespace\n' )
    root = Node( 'root', inNamespace=False )
    rootintf = net.addLink( root, s1 ).intf1
    root.setIP( '10.10.10.10/8', intf=rootintf )

    net.start()

    # Add route for multicast range 230.0.0.0/8 on hosts
    info( '*** Adding route for multicast range 230.0.0.0/8\n' )
    for h in net.hosts:
        routecmd = 'route add -net 230.0.0.0 netmask 255.0.0.0 dev ' + h.defaultIntf().name
        info( h.name + '> ' + routecmd + '\n' )
        h.cmd( routecmd )

    try:
        CLI( net )
    except KeyboardInterrupt:
        info( 'Oops, caught a KeyboardInterrupt...' )
    finally:
        net.stop()
