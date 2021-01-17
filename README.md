# 4.1 – Pub-Sub Routing

- write a "subscriber" application (pref. Python or Java) which receives UDP datagrams containing measurement data and
possibly filters them by measurement type (0 for energy and 1 for power) and value (greater, less or equal to a certain reference value)
    + Subscriber input parameters: 
        - UDP port
        - measurement type
        - comparison operator
        - reference value
        - whether to filter at all
        
- Write a script ~/ex4/task41.sh which install flows for: 
    - ARP broadcast and IP forwarding in the 10.0.0.0/8 network
    - forwarding of measurements (matched by the multicast IP 230.0.0.0/8) to all subscribers
- Compare the size of the unfiltered vs. filtered output
- UDP programming reference: https://www.baeldung.com/udp-in-java
```
java Subscriber 50001 1 500 all gt
java Subscriber 50002 1 100 all gt
java Subscriber 50003 0 30 all st
java Subscriber 50004 0 136 all gt
```

# 4.2 – Content-based Routing

# 4.3 – REST Interface for Content-based Routing

- Create a package net.sdnlab.ex4.task43 for a Floodlight Module
providing the following REST interface:
- Java JSON: http://tutorials.jenkov.com/java-json/index.html

```
ssh sdnfp04_proxy

sshfs sdnfp04_proxy:/home/student/ex4 remote_sshfs_ex4

sshfs sdnfp04_proxy:/opt/floodlight/src/main/java/net/sdnlab/ex4 remote_java_ex4

sudo reboot
```

Others
- https://medium.com/altcampus/how-to-merge-two-or-multiple-git-repositories-into-one-9f8a5209913f#id_token=eyJhbGciOiJSUzI1NiIsImtpZCI6IjI1MmZjYjk3ZGY1YjZiNGY2ZDFhODg1ZjFlNjNkYzRhOWNkMjMwYzUiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJuYmYiOjE2MTA1NTg4NDcsImF1ZCI6IjIxNjI5NjAzNTgzNC1rMWs2cWUwNjBzMnRwMmEyamFtNGxqZGNtczAwc3R0Zy5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsInN1YiI6IjEwODcyMTYzNTA3OTY4MjM2Nzc2NiIsImVtYWlsIjoia3Vhbmd5dS5sdW1pZXJlLmxpQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhenAiOiIyMTYyOTYwMzU4MzQtazFrNnFlMDYwczJ0cDJhMmphbTRsamRjbXMwMHN0dGcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJuYW1lIjoiS1VBTkctWVUgTEkiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1iLW1RNkpzNVpxSS9BQUFBQUFBQUFBSS9BQUFBQUFBQUFBQS9BTVp1dWNsWEtZb0pvZm9WUkI3SkRJcnJLNUJRblJXdFlBL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJLVUFORy1ZVSIsImZhbWlseV9uYW1lIjoiTEkiLCJpYXQiOjE2MTA1NTkxNDcsImV4cCI6MTYxMDU2Mjc0NywianRpIjoiZjg2Y2E5N2FlMmJkYTI4ZTIwY2Q0YTJhNzM0NjQwOWMxNDFjNmFmYiJ9.A-rlY3t9fEImYYPBmX6qQ_1oJR27DGIgOaSrvE7D37pAbceTytcU9gm8VqKgwv5bJkannqE8AK0mBXB793FU98sukC43Y0llssvyZRaYZvXSAckfaPwHgAJqr49HJOWXha7HKXgdluHBDU0pDqm-xeJtYaGFgiZ-pWQ-xyxiNSgBtQa9tqjjkTZ42Oee62iJ_UEGvoP18wQH4YQbf5HcLMPhDfqqdRAWw8VCtgD4HtLdO4zH_Wz9evDnV73nacD8Q9B5auChkLLivf3DRM3ImfgDZWG2nb19BS9sS_d36GN_EMvtoHjx78KqXtjM7_9TOGJjtWwHk-9zempcK9F0iw
- https://www.jetbrains.com/help/idea/working-with-code-documentation.html#auto-comment
 /** before a declaration and press ⏎