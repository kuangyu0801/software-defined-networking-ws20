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

def main():
    return monitor
