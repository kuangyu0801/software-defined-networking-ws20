################################################################################
# Implement the Quality-of-Service module                                      #
#                                                                              #
# $./pyretic.py -v low -m r0 qos                                               #
################################################################################

from pyretic.lib.corelib import *
from pyretic.lib.std import *
from pyretic.lib.query import *

ip_h1 = IPAddr('10.0.0.1')
ip_h2 = IPAddr('10.0.0.2')
ip_mon = IPAddr('10.0.0.3')
ip_srv = IPAddr('10.0.0.4')
ip_inet = IPAddr('10.0.0.5')
# h1-srv, h1-inet
policy_h1_pass = (match(srcip=ip_h1, dstip=ip_srv) >> fwd(4)) + (match(srcip=ip_h1, dstip=ip_inet) >> fwd(5))
# h2-srv, h2-inet
policy_h2_pass = (match(srcip=ip_h2, dstip=ip_srv) >> fwd(4)) + (match(srcip=ip_h2, dstip=ip_inet) >> fwd(5))

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
	#forwardPolicy = match(dstport=4000) >> fwd(4)
	return qos()


