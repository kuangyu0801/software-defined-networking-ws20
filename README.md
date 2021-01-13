# 4.1 – Pub-Sub Routing

- write a "subscriber" application (pref. Python or Java) which receives UDP datagrams containing measurement data and
possibly filters them by measurement type (0 for energy and 1 for power) and value (greater, less or equal to a certain reference value)

- Write a script ~/ex4/task41.sh which install flows for: 
    - ARP broadcast and IP forwarding in the 10.0.0.0/8 network
    - forwarding of measurements (matched by the multicast IP 230.0.0.0/8) to all subscribers
- Compare the size of the unfiltered vs. filtered output

# 4.2 – Content-based Routing

# 4.3 – REST Interface for Content-based Routing

- Create a package net.sdnlab.ex4.task43 for a Floodlight Module
providing the following REST interface:

```
ssh sdnfp04_proxy

sshfs sdnfp04_proxy:/home/student/ex4 remote_sshfs_ex4

sshfs sdnfp04_proxy:/opt/floodlight/src/main/java/net/sdnlab/ex4 remote_java_ex4

sudo reboot
```