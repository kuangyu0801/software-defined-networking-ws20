from pyretic.lib.corelib import *


forwardBlue = (match(switch=1) | match(switch=2) | match(switch=4) | match(switch=6) | match(switch=8)) & match(inport=1) >> fwd(2)

def main():
    return forwardBlue
