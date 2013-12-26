import sys
f = open("short.out",'r')
line = f.readline().strip()
data = []
while len(line) > 0:
	line = f.readline().strip()
	data.append(line)
	line = f.readline().strip()
for d in data:
	if len(d) > 0:
		print d