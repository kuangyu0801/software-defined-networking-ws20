from pyretic.lib.corelib import *
from pyretic.core import packet
from pyretic.lib.query import *

ip_h1 = IPAddr('10.0.0.1')
ip_h2 = IPAddr('10.0.0.2')
ip_mon = IPAddr('10.0.0.3')
ip_srv = IPAddr('10.0.0.4')
ip_inet = IPAddr('10.0.0.5')

def firewall():
    # broadcast arp request and arp reply
    arp = match(ethtype=ARP_TYPE) >> flood()
    # inet - srv8080, h2 - srv8080, h1 - srv&inet
    all_to_srv_web = match(dstip=ip_srv, dstport=8080, ethtype=packet.IPV4, protocol=packet.TCP_PROTO) >> fwd(4)
    srv_to_h1 = match(srcip=ip_srv, dstip=ip_h1) >> fwd(1)
    srv_to_h2 = match(srcip=ip_srv, dstip=ip_h2) >> fwd(2)
    srv_to_inet = match(srcip=ip_srv, dstip=ip_inet) >> fwd(5)
    h1_to_srv_ssh = match(srcip=ip_h1, dstip=ip_srv, dstport=9090, ethtype=packet.IPV4, protocol=packet.TCP_PROTO) >> fwd(4)
    h1_to_inet = match(srcip=ip_h1, dstip=ip_inet, dstport=8080, ethtype=packet.IPV4, protocol=packet.TCP_PROTO) >> fwd(5)
    inet_to_h1 = match(srcip=ip_inet, dstip=ip_h1) >> fwd(1)

    firewallPolicy = all_to_srv_web + srv_to_h1 + srv_to_h2 + srv_to_inet + h1_to_srv_ssh + h1_to_inet + inet_to_h1 + arp
    return firewallPolicy

def monitor():
    # mon prints every filtered traffic
    return fwd(3)

def main():
    return (firewall() >> monitor()) + firewall()
