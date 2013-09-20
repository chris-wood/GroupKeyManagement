import sys

# params
fname = sys.argv[1]

# parsing
times = {}
fn = open(fname, 'r')
for line in fn:
	line = line.strip()
	if "," in line and "=" not in line: # skip debug lines
		data = line.split(",")
		for i in range(len(data)):
			data[i] = data[i].strip()

		# Retrieve the parameters for this simulation
		print(data)
		k = int(data[0])
		m = int(data[1])
		n = int(data[2])
		p1 = float(data[3])
		p2 = float(data[4])
		time = float(data[5])
		stddev = float(data[6])
		stderr = float(data[7])

		# Group by k,m,p1,p2 (keyed tuple) and store tuple (n, time, stddev, stderr)
		if not ((k,m,p1,p2) in times):
			times[(k,m,p1,p2)] = []
		times[(k,m,p1,p2)].append((n, time, stddev, stderr))

# print times
for tup in times:
	fname = "data_" + str(tup[0]) + "_" + str(tup[1]) + "_" + str(tup[2]) + "_" + str(tup[3])
	fout = open(fname, 'w')
	for data in times[tup]:
		fout.write(str(data[0]) + "," + str(data[1]) + "," + str(data[2]) + "," + str(data[3]) + "\n")
