################################################################################
# Implement the monitoring firewall with QoS                                   #
#                                                                              #
# $./pyretic.py -v low -m r0 monitor_firewall_qos                              #
################################################################################

from pyretic.lib.corelib import *
from pyretic.core import packet
from pyretic.lib.query import *

ip_h1 = IPAddr('10.0.0.1')
ip_h2 = IPAddr('10.0.0.2')
ip_mon = IPAddr('10.0.0.3')
ip_srv = IPAddr('10.0.0.4')
ip_inet = IPAddr('10.0.0.5')
policy_h1_pass = (match(srcip=ip_h1, dstip=ip_srv) >> fwd(4)) + (match(srcip=ip_h1, dstip=ip_inet) >> fwd(5))
policy_h2_pass = (match(srcip=ip_h2, dstip=ip_srv) >> fwd(4)) + (match(srcip=ip_h2, dstip=ip_inet) >> fwd(5))

def firewall():
	# broadcast arp request and arp reply
	arp = match(ethtype=ARP_TYPE) >> flood()
	# inet - srv8080, h2 - srv8080, h1 - srv&inet
	all_to_srv_web = match(dstip=ip_srv, dstport=8080, ethtype=packet.IPV4, protocol=packet.TCP_PROTO) >> fwd(4)
	srv_to_h1 = match(srcip=ip_srv, dstip=ip_h1) >> fwd(1)
	srv_to_h2 = match(srcip=ip_srv, dstip=ip_h2) >> fwd(2)
	srv_to_inet = match(srcip=ip_srv, dstip=ip_inet) >> fwd(5)
	h1_to_srv_ssh = match(srcip=ip_h1, dstip=ip_srv, dstport=9090, ethtype=packet.IPV4, protocol=packet.TCP_PROTO) >> fwd(4)
	h1_to_inet = match(srcip=ip_h1, dstip=ip_inet, dstport=8080, ethtype=packet.IPV4, protocol=packet.TCP_PROTO) >> fwd(5)
	inet_to_h1 = match(srcip=ip_inet, dstip=ip_h1) >> fwd(1)
	firewallPolicy = all_to_srv_web + srv_to_h1 + srv_to_h2 + srv_to_inet + h1_to_srv_ssh + h1_to_inet + inet_to_h1 + arp	
	return firewallPolicy

def monitor():
	# mon prints every access to the ssh service on srv as well as all traffic from h2
	return fwd(3)

class qos(DynamicPolicy):
	def __init__(self):
		super(qos,self).__init__()
		self.query = count_packets(0.05,['srcip'])
		self.query.register_callback(self.packet_count_printer)
		self.policy_h1 = policy_h1_pass
		self.policy_h2 = policy_h2_pass
		self.policy = self.policy_h1 + self.policy_h2 + self.query

	def packet_count_printer(self,counts):
		if counts:
			#print counts[match(srcip = IPAddr('10.0.0.1'))]
			#print "----counts------"
			for group, count in counts.iteritems():
				print group, 'count :' , count
				if (group==match(srcip = ip_h1)):
					if (count % 10 == 0):
						self.policy_h1 = match(srcip=ip_h1) >> drop
					else:
						self.policy_h1 = policy_h1_pass
				elif (group==match(srcip = ip_h2)):
					if (count % 5 == 0):
						self.policy_h2 = match(srcip=ip_h2) >> drop
					else:
						self.policy_h2 = policy_h2_pass
				self.policy = self.policy_h1 + self.policy_h2 + self.query	
def main():
	monitor_firewall = (firewall() >> monitor()) + firewall()
	# only apply qos to UDP packet
	return if_(match(protocol=packet.UDP_PROTO), qos(), monitor_firewall)
