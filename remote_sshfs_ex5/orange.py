from pyretic.lib.corelib import *


forwardOrange = (match(switch=1,inport=1) >> fwd(4)) + ((match(switch=3,inport=1) | match(switch=4,inport=4)) >> fwd(3)) + (match(switch=7,inport=3) >> fwd(1)) + (match(switch=8,inport=3) >> fwd(2))

def main():
    return forwardOrange