
# OpenFlow
   # RESTful API
   # Module Interface 

# MiniNet

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
 
 # Pyretic Platform
   - declarative network programming: defines desired network behavior
   - Key Concept: Policy: mapping a located packet
        + Describes semantics of network What to do with an incoming packet?
            + Parallel
            + Sequential
            + Filter Policies (Predicates)
        + Query Policies: Pushing data from the data plane to the controller (“monitoring”)
    + Defined behavior to OpenFlow rules at runtime
    + Compiler chooses
        + header structure dependency
        + data structure mapping
        + OF-action dependency
        