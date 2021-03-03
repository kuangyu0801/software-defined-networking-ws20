- google pdf: https://docs.google.com/document/d/1kqtkNWKxlSD0TE3AhZ5L8_JII6Ko6dPyA5zIose2wmI/edit?usp=sharing




# Task 5.1
- Implement your firewall / monitor solution within the provided files
- Test each module independently, start your pyretic module
```
pyretic.py -v low -m r0 simple_firewall
```

- Test your solution by bringing up a Mininet

On h1, h2, and inet
```
curl –sS –m 5 10.0.0.X:Y0Y0,
```
On mon
```
tcpdump
```

-s/--silent
Silent or quiet mode. Don't show progress meter or error messages. Makes Curl mute.
-S/--show-error
When used with -s it makes curl show an error message if it fails.
# Task 5.2
```
pyretic.py –v low –m r0 qos
```

On inet and srv, run
```
./udpreceiver 4000,
```
On inet and srv
```
./udpsender 10.0.0.X 4000 300,
```

# Task 5.3
```
pyretic.py –v low –m r0 monitor_firewall

sudo ./mininet5.py mininet

> startservers mininet

> xterm h1 h2 srv inet mon
```
On h1, h2, and inet access web & ssh services through:
```
curl –sS –m 5 10.0.0.X:Y0Y0
```
# Task 5.4

# Reference

- pyretic manual: https://github.com/frenetic-lang/pyretic/wiki/Language-Basics