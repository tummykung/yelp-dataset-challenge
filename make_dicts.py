import sys

def make_dicts(filename):
	data = []

	with open(filename) as f:
		for line in f.readlines():
			line = line.replace('true','True').replace('false','False').strip()
			try:
				data.append(eval(line))
			except:
				print 'Error occured with line\n%s'%line

	return data


if __name__ == '__main__':
	try:
		filename = sys.argv[1]
	except:
		filename = 'yelp_academic_dataset_business.json'

	print len(make_dicts(filename))

