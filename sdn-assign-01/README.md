
# PDF
[https://docs.google.com/document/d/1p2vxC7d9hbvbzbkAPysfdV7cdm_mX_MZ20ytWE5uPys/edit?usp=sharing][google docs]
# 知識點釐清
- arp包長怎樣
[http://www.networksorcery.com/enp/protocol/arp.htm][arp header]
- ip packet
![avatar](picture/ip-packet-header.png)
![avatar](picture/ip-protocol-number.png)
- ethernet frame
![avatar](picture/ethernet-frame.png)
- 實際上vlan是靠什麼控制的？
# Task1.1
- ping代表什麼意思？實際執行的是什麼?
Ping operates by sending **Internet Control Message Protocol (ICMP)** echo request packets to the target host and waiting for an **ICMP echo reply**. 

- what is ICMP?

three pings from host 1 to host 2

# Task1.2
- 如果arp廣播的src本身不在接收到的host arp table裡面, 就不會self learning

# Task1.3 Extended VLANs
- 這題不是要我們去實作arp，也不要我們利用vlan去控制封包轉法(因為h3, h4會在同一個網域,flooding會有問題！)
- 助教特別提醒我們就是要綁mac

- LAN vs. VLAN: 多個VLAN可以架在同一個physical LAN上面，透過在邊緣push, pop VLAN ID來判斷轉發的frame
- 必須先執行, 才能install flow with REST-API
/opt/floodlight/floodlight-noforwarding.sh

- ARP是屬於哪一層的？L2
- EtherType is a two-octet field in an Ethernet frame. It is used to indicate which **protocol is encapsulated** in the **payload of the frame** and is used at the receiving end by the **data link layer** to determine how the payload is processed. The same field is also used to indicate the size of some Ethernet frames.
用來表示frame裡面的payload的type
  * 0x0800	Internet Protocol version 4 (IPv4) : ping packet是屬於這個！
  * 0x0806:	Address Resolution Protocol (ARP)
  * 0x86DD	Internet Protocol Version 6 (IPv6)
  
# Task1.4 Python API and ARP Caches
mininet.net.Mininet.staticArp()
Add all-pairs ARP entries to remove the need to handle broadcast.

# 如何連到機器

## 方法一(推薦）：直接設定ssh搭配sshfs(一勞永逸)

新增公鑰讓連到ipvs機器以後都不需要密碼
```
ssh-copy-id liku@ipvslogin.informatik.uni-stuttgart.de
```

接著系統會要求你輸入ipvs密碼，連進去之後就直接會是sdnfp04

新增.ssh/config讓ssh可以透過ipvslogin當作proxy直接連到sdnfp04
```
Host sdnfp04_proxy
        HostName sdnfp04
        User student
        ForwardAgent yes
        ForwardX11 yes
        ProxyJump proxy
Host proxy
        HostName ipvslogin.informatik.uni-stuttgart.de
        User liku
        ForwardAgent yes
        ForwardX11 yes
```
在.ssh/下執行ssh，直接登入，
```
ssh sdnfp04_proxy
```
mount遠端硬碟設定
```
sshfs sdnfp04_proxy:/home/student/ex1 remote_sshfs_ex1
```
不需要使用的時候
```
unmount remote_sshfs_ex1
```
## 方法二：透過X2G0
- x2go設定
![avatar](/picture/x2go_config.png)
- 成功登入畫面
![avatar](/picture/x2go.png)

## 方法三（基本）：兩次ssh（一通百通）

0. 只需要做一次) 
用sshfs把自己本機的公鑰放到ipvslogin的遠端server裡面
```
sshfs liku@ipvslogin.informatik.uni-stuttgart.de:\home\liku remote_sshfs
```

1. 不用連到informatik也可以
```
ssh liku@ipvslogin.informatik.uni-stuttgart.de
```
接著輸入系統要求的密碼

2. 從ipvslogin跳到sdnfp04.infra.informatik.uni-stuttgart.de

```
ssh -i #your_public_key student@sdnfp04
```

3. 成功使用公鑰登入！
成功授權的公鑰是放在authorized_key這個檔案裡面，打開就可以檢視
```
vim .ssh/authorized_key
```

# Note
- XTerm terminal(配合x2go使用)
- dump right away


# Reference
- Introduction to MiniNet [https://github.com/mininet/mininet/wiki/Introduction-to-Mininet#what][3]
- 鳥哥的基礎網路觀念: [http://linux.vbird.org/linux_server/0110network_basic.php#tcpip_network_arp][4]
- 重要！！mini net walkthrough http://mininet.org/walkthrough/
- Dpctl Documentation https://github.com/CPqD/ofsoftswitch13/wiki/Dpctl-Documentation
- https://man7.org/linux/man-pages/man8/arp.8.html

- Computer Networks 清大課程
- youtube playlist: [https://www.youtube.com/playlist?list=PL9jciz8qz_zxeXaMgy4Z77P7dKNRWegE0][5]
- 中繼器 (Repeater)、集線器 (Hub)、橋接器 (Bridge)、交換器 (Switch) 原理與介紹
- 開放式課程官網 : [http://ocw.nthu.edu.tw/ocw/index.php?page=course&cid=13&v][6]
- 官網投影片：[http://gaia.cs.umass.edu/kurose-ross-ppt-6e/][7]

# Open Issue

- [https://www.ssh.com/ssh/config/][2] 
- LAN vs. VLAN
- ~~waiting for password to access VM~~
- mini net支持

- 你該知道所有關於 SSH 的那些事 [https://jennycodes.me/posts/security-ssh][1]

[1]: https://jennycodes.me/posts/security-ssh
[2]: https://www.ssh.com/ssh/config/
[3]: https://github.com/mininet/mininet/wiki/Introduction-to-Mininet#what

[4]: http://linux.vbird.org/linux_server/0110network_basic.php#tcpip_network_arp

sudo mn --mac --switch ovsk --controller remote,port=6653 --custom ex1/task12topo.py --topo task12topo
dpctl dump-flows

- install之後就算是mn clean up 
# 常用linux command
- arp

[5]: https://www.youtube.com/playlist?list=PL9jciz8qz_zxeXaMgy4Z77P7dKNRWegE0

[6]: http://ocw.nthu.edu.tw/ocw/index.php?page=course&cid=13&v

[7]: http://gaia.cs.umass.edu/kurose-ross-ppt-6e/

[arp header]: http://www.networksorcery.com/enp/protocol/arp.htm

[google docs]: https://docs.google.com/document/d/1p2vxC7d9hbvbzbkAPysfdV7cdm_mX_MZ20ytWE5uPys/edit?usp=sharing