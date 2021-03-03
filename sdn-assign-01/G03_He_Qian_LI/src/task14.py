#!/usr/bin/python

# Dependencies
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.cli import CLI
from mininet.node import OVSSwitch, RemoteController
from mininet.log import setLogLevel, info

# Enable logging to the terminal
setLogLevel( 'info' )

# Initialize Mininet
net = Mininet( switch=OVSSwitch, controller=RemoteController, build=False )
# Add remote controller
net.addController( 'c0', port=6653 ) # use Floodlight's default OF port

#! DONE: add hosts
h11=net.addHost('h11', ip='10.10.1.1/8', mac='00:00:00:00:00:11')
h12=net.addHost('h12', ip='10.10.1.2/8', mac='00:00:00:00:00:12')
h13=net.addHost('h13', ip='10.10.1.3/8', mac='00:00:00:00:00:13')

h21=net.addHost('h21', ip='10.10.2.1/8', mac='00:00:00:00:00:21')
h22=net.addHost('h22', ip='10.10.2.2/8', mac='00:00:00:00:00:22')
h23=net.addHost('h23', ip='10.10.2.3/8', mac='00:00:00:00:00:23')

h41=net.addHost('h41', ip='10.10.4.1/8', mac='00:00:00:00:00:41')
h42=net.addHost('h42', ip='10.10.4.2/8', mac='00:00:00:00:00:42')
h43=net.addHost('h43', ip='10.10.4.3/8', mac='00:00:00:00:00:43')

#! DONE: add switches
s1=net.addSwitch('s1')
s2=net.addSwitch('s2')
s3=net.addSwitch('s3')
s4=net.addSwitch('s4')

#! DONE: add links
net.addLink(h11, s1)
net.addLink(h12, s1)
net.addLink(h13, s1)

net.addLink(h21, s2)
net.addLink(h22, s2)
net.addLink(h23, s2)

net.addLink(h41, s4)
net.addLink(h42, s4)
net.addLink(h43, s4)

net.addLink(s1, s2)
net.addLink(s2, s3)
net.addLink(s3, s4)

# Start Mininet
info( '=== Starting Mininet ===\n' )
net.build()
net.start()

#! DONE: fill ARP caches of all hosts
net.staticArp()
# Start CLI
CLI( net )
# When user exits CLI, stop Mininet
info( '=== Stopping Mininet ===\n' )
net.stop()

