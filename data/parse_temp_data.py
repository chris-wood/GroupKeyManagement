import sys

# params
fname = sys.argv[1]
k = sys.argv[2]
m = sys.argv[3]
p1 = sys.argv[4]
p2 = sys.argv[5]

# parsing
times = []
fn = open(fname, 'r')
for line in fn:
	line = line.strip()
	if "," in line:
		data = line.split(",")
		for i in range(len(data)):
			data[i] = data[i].strip()
		if data[0] == k and data[1] == m and data[3] == p1 and data[4] == p2:
			print >> sys.stderr, line
			times.append(data[5])

# print times
print "k m p1 p2"
print str(k) + " " + str(m) + " " + str(p1) + " " + str(p2)
for t in times:
	print(t)
