
# Software-defined Networking
## Motivation:
+ Limited flexibility: Network protocols seem to be “hard-coded“
+ Separation of network and application
+ Integrated system view would benefit application and network!
+ Higher performance of application
+ Higher efficiency and utilization of network
## Benefits of SDN
+ Easy modification of the network control logic
+ API to “program” the network
+ High-level programming languages (Java Python vs. C, RTL)
+ Reduced switch complexity
+ Integrated system: application & network
+ High performance of forwarding utilizing hardware switches
+ Reducing the complexity of implementing control logic: Distribution transparency
## SDN in a Nutshell: SDN is a paradigm to program networks at a high-level
+ Flow-based Forwarding (not only certain layer, but bundled layer)
+ Control Plane and Data Plane Separation
    +Control plane: defines routes, manages network graph
    + Data plane: forwarding of packets
+ Logically Centralized Controller
    + logically centralized
        + simplifies implementation of control logic
        + increase global view
    + physically distributed
        + ensures high availability and scalability
## Architecture of an SDN System
+ Switches/Routers
+ SDN Controller
    + Implements control plane, southbound interface
        + has Interfaces with control logic (control “application”) via northbound
              interface(s)
        + Physical distribution is transparent to control logic
        + No standard way of distribution defined
+ Control Logic

# OpenFlow
   - OpenFlow de facto standard for southbound interface
   - Proactively: before the flow starts
        + Pro: Reduces controller load
        + Con: Occupies space in flow table of switch (which is normally limited)
   - Reactively: as soon as the flow starts
        + Pro: Saves flow table space
        + Con: Puts load onto controller and control network (big issue for UDP)
# RESTful API
# Module Interface 

# MiniNet
   - Mininet creates realistic virtual networks on a single machine
        + Virtual OpenFlow switches
        + Virtual hosts
        + Virtual network interfaces
# Network Programming with OpenFlow: No control plane abstractions
- Pro
    - Network-wide visibility
    - Direct control over the switches
    - Simple data-plane abstraction
- Con
    - Low-level programming interface
    - Functionality tied to hardware
    - Explicit resource control
    - Challenging distributed programming
    
 # Desired Abstractions: Behavior
   - Forwarding abstraction
   - Specification abstraction
   - State distribution abstraction
 # Traffic Statistics and Topology Discovery
 ##  Traffic Statistics
    - OpenFlow switches implement counters
    - Controller can query counters
## Topology Discovery
    - Switch discovery:  OpenFlow protocol
    - Host discovery: Address Resolution Protocol (ARP)
    - Links between switches: Link Layer Discovery Protocol (LLDP)
 # Pyretic Platform 
   - declarative network programming: defines desired network behavior
   + Pyretic Controller Platform (POX): Defined behavior to OpenFlow rules at runtime
       + Compiler chooses
           + header structure dependency
           + data structure mapping
           + OF-action dependency
   - Key Concept: Policy: mapping a located packet
        + Describes semantics of network What to do with an incoming packet?
            + Parallel
            + Sequential
            + Filter Policies (Predicates)
        + Query Policies: Pushing data from the data plane to the controller (“monitoring”)
    + Pyretic Runtime’s Modes of Operation
        + Interpreted (on controller): runtime, good for debugging
        + Reactive: Fallback when proactive isn’t feasible
        + Proactive
        
    
        