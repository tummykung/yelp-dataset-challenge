import matplotlib.pyplot as plt
import matplotlib.dates as mdates
import pickle
import datetime as dt
import time

class Rev:
	USER_ID, BUS_ID, DATE, STARS, FUNNY, USEFUL, COOL = range(7)
	string =['user_id', 'business_id', 'date', 'stars', '']

"""
reviews in format
{"votes": {"funny": 0, "useful": 5, "cool": 2},
 "user_id": "rLtl8ZkDX5vH5nAx9C3q5Q",
 "review_id": "fWKvX83p0-ka4JS3dc6E5A",
 "stars": 5,
 "date": "2011-01-26",
 "text": "blahblahblah",
 "type": "review",
 "business_id": "9yKzy9PApeiPPOUJEtnvkg"}
 """

def get_pickled_data(s='business'):
	"""
	returns data from file
	use strings: 'business', 'checkin', 'review', 'user'
	""" 
	filename = s+'_pickled.txt'
	with open(filename) as f:
		return pickle.load(f)

def make_review_data():
	reviews = get_pickled_data('review')
	#put reviews into list format
	review_list = [[rev['user_id'], rev['business_id'],rev['date'], rev['stars'],
					rev['votes']['funny'], rev['votes']['useful'], rev['votes']['cool']] for rev in reviews]
	businesses = {} #id:data dict
	for rev in review_list:
		if rev[Rev.BUS_ID] in businesses:
			businesses[rev[Rev.BUS_ID]].append(rev)
		else:
			businesses[rev[Rev.BUS_ID]] = [rev]

	data = list(zip(*businesses.items())[1])
	data.sort(key=lambda k: len(k), reverse=True)
	return data


def date_to_secs(date):
	try:
		t = dt.datetime.strptime(date, '%Y-%m-%d')
	except:
		print date
		a = 1/0
	return time.mktime(t)

def date_to_days(date):
	return mdates.datestr2num(date)

def extract_time(review):
	return date_to_days(review[Rev.DATE])

def trailing_avg(lst, num=5):
	smooth_list = [sum(lst[max(0,i-num):i])*1.0/len(lst[max(0,i-num):i]) for i in range(1, len(lst)+1)]
	return smooth_list

def plot(data,x,y):
	vals = [(d[x], d[y]) for d in data]
	plt.scatter(zip(*vals))
	plt.show()
	return vals

def get_business_info(bus_id, business_data=[]):
	if len(business_data) == 0:
		business_data.extend(get_pickled_data('business'))
	for b in business_data:
		if b['business_id'] == bus_id: return b

def graph_over_time(data, feature=Rev.STARS, smoothing=20):
	times = map(extract_time, data)
	ratings_orig = [rev[feature] for rev in data]
	ratings = trailing_avg(ratings_orig, num=smoothing)

	plt.plot_date(times, ratings, xdate=True)
	plt.ylim(0,5)
	plt.show()


if __name__ == '__main__':
	with open('Ordered_review_data.pickle') as review_file:
		review_data = pickle.load(review_file)
	if False:
		with open('Ordered_review_data.pickle', 'w') as review_file:
			for business in review_data:
				business.sort(key=lambda k: k[Rev.DATE])
			pickle.dump(review_data, review_file)

	if False:
		first_business_data = review_data[0]
		times = map(extract_time, first_business_data)
		ratings_orig = [rev[Rev.STARS] for rev in first_business_data]
		ratings = trailing_avg(ratings_orig)
		plt.plot(times, ratings)
		plt.show()

	for i in range(1,20):
		print get_business_info(review_data[i][0][Rev.BUS_ID])
		graph_over_time(review_data[i])
