################################################################################
# Implement the simple monitor module                                          #
#                                                                              #
# $./pyretic.py -v low -m r0 simple_monitor                                    #
################################################################################

from pyretic.lib.corelib import *
from pyretic.core import packet
from pyretic.lib.query import *

#mac1 = EthAddr('C0:FF:EE:00:BA:BE')
#ip1  = IPAddr('123.45.67.89')
ip_h2 = IPAddr('10.0.0.2')
ip_srv = IPAddr('10.0.0.4')
# DONE
# mon prints every access to the ssh service on srv as well as all traffic from h2
ssh_to_mon = match(dstip=ip_srv, dstport=9090, ethtype=packet.IPV4, protocol=packet.TCP_PROTO) >> fwd(3)
h2_to_mon = match(srcip=ip_h2) >> fwd(3)
monitor = ssh_to_mon + h2_to_mon
def main():
    return monitor
