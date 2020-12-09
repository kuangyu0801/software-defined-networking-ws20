# Task 1

# Task 2

## Task2.1
h1, h2都收不到udp packet
- 可能方向是從udp deliver去查找

實驗1:使用default forwarding也沒有辦法ping成功 
- 可能原因, 兩個屬於不同網段(搜尋：同个网段和不同网段的ping过程)
- 檢查arp table
http://linux.vbird.org/linux_server/0230router.php

Per-packet Consistency

機制：stamps packets with their configuration version at ingress switches and tests for the version number in all other rules.
## Step 1
- 1. controller first pre-processes the rules in the new configuration, augmenting the pattern of each rule to match the new version number in the header. 
- 2. Next, it installs these rules on all of the switches, leaving the rules for the old configuration (whose rules match the previous version number) in place.
- 3. The controller then starts updating the ingress switches, replacing their old rules with new rules that stamp incoming packets with the new version number. 
- 4. once all packets following the “old” policy have left the network, the controller deletes the old configuration rules from all switches, completing the update
還可以量converge的時間! (responsiveness) 從new rule applied到最舊的packet離開整個networt


# Task 3

- https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/1343513/How+to+Write+a+Module
- https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/9142279/How+to+Process+a+Packet-In+Message
- https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/9142281/How+to+Create+a+Packet+Out+Message
- [Switch Flow Table Configuration](https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/9142325/Switch+Flow+Table+Configuration)
- [Floodlight Javadoc](http://floodlight.github.io/floodlight/javadoc/floodlight/index.html)
- 1. controller設hardtimeout, 時間一到把所有包從s1轉給自己, 然後再install到s8上面
- // where does log comes from and how to generate log

直接看特定switch json

```
curl http://<controller_ip>:8080/wm/staticentrypusher/list/00:00:00:00:00:00:00:01/json
```

# 知識點釐清
- ARP請求會不會對全外網進行廣播

# Nice to Have
- Java logger https://www.journaldev.com/977/logger-in-java-logging-example
```
ssh sdnfp04_proxy

sshfs sdnfp04_proxy:/home/student/ex2 remote_sshfs_ex2
sshfs sdnfp04_proxy:/opt/floodlight/src/main/java/net/sdnlab/ex2 remote_java_ex2
sshfs sdnfp04_proxy:/opt/floodlight/src/main/resources/META-INF/services remote_meta_ex2

/opt/floodlight/floodlight-noforwarding.sh
dpctl dump-flows
```

[]: https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/9142325/Switch+Flow+Table+Configuration

[]: http://floodlight.github.io/floodlight/javadoc/floodlight/index.html