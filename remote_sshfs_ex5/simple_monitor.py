################################################################################
# Implement the simple monitor module                                          #
#                                                                              #
# $./pyretic.py -v low -m r0 simple_monitor                                    #
################################################################################

from pyretic.lib.corelib import *

mac1 = EthAddr('C0:FF:EE:00:BA:BE')
ip1  = IPAddr('123.45.67.89')

# TODO
monitor = flood()

def printer(pkt): 
    print pkt
    
queryPolicy = packets() 
queryPolicy.register_callback(printer)
    
def main():
    return match(dstip=ip_srv, dstport=9090, ethtype=packet.IPV4, protocol=packet.TCP_PROTO) | match(scrip=ip_h2) >> queryPolicy
