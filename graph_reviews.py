import matplotlib.pyplot as plt
import matplotlib.dates as mdates
import pickle
import datetime as dt
import time
import random
from math import log

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

def get_times(reviews):
	return [extract_time(rev) for rev in reviews]

def get_ratings(reviews):
	return [rev[Rev.STARS] for rev in reviews]

def trailing_avg(lst, num=5):
	smooth_list = [sum(lst[max(0,i-num):i])*1.0/len(lst[max(0,i-num):i]) for i in range(1, len(lst)+1)]
	return smooth_list

def binning_avg(time, data, dt=30):
	time_thresh = time[0]+dt
	binned_data = [[]]
	binned_time = [time_thresh]
	step=0
	for d,t in zip(data,time):
		if t < time_thresh:
			binned_data[step].append(d)
		else:
			while t >= time_thresh:
				time_thresh += dt
			binned_time.append(time_thresh)
			binned_data.append([])
			step += 1
			binned_data[step].append(d)

	binned_data = [sum(d)*1.0/len(d) for d in binned_data]

	return binned_time, binned_data


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
	#times, ratings = binning_avg(times, ratings_orig, smoothing)

	plt.plot_date(times, ratings, 'o-',xdate=True)
	plt.ylim(0,5)
	plt.show()

def graph_corrs_over_time(reviews, smoothing=20):
	times = get_times(reviews)
	ratings = get_ratings(reviews)
	smoothed_ratings = trailing_avg(ratings, smoothing)
	plt.subplot(3,1,1)
	plt.plot_date(times, smoothed_ratings)
	plt.subplot(3,1,2)

def get_month_counts(business_data):
	"""counts[month[1-12]]=(review count, mean)"""
	month_data = [[] for i in range(13)]
	for review in business_data:
		date = dt.datetime.strptime(review[Rev.DATE],'%Y-%m-%d')
		month = date.month
		rating = review[Rev.STARS]
		#limit to particular year
		#if date.year == 2012:
		month_data[0].append(rating)
		month_data[month].append(rating)
	counts = [(len(revs), sum(revs)*1.0/len(revs)) for revs in month_data]
	return counts, month_data[0]

def mean(lst):
	return sum(lst)*1.0/len(lst)

def list_add(lst,n):
	return [l+n for l in lst]

def monte_carlo(data, n):
	"""choose n from data with replacement, return mean"""
	return mean([random.choice(data) for i in range(n)])


def monte_carlo_months(business_data, reps=1000):
	orig_avgs, orig_data = get_month_counts(business_data)
	#print "orig_avgs:\n%s" %str(orig_avgs)
	#print "orig_data:\n%s" %str(orig_data)
	sim_data = [[monte_carlo(orig_data, n) for i in range(reps)] for (n, mu) in orig_avgs[1:]]
	#print "sim_data:\n%s" %str(sim_data)
	pvals = []
	for sim, orig in zip(sim_data, orig_avgs[1:]):
		#print "mu: %g"%orig[1]
		count = 0
		if orig[1] > orig_avgs[0][1]:
			for s in sim:
				if s >= orig[1]:
					count += 1
		else:
			for s in sim:
				if s <= orig[1]:
					count += 1
		pvals.append(count*1.0/reps)
	mus = [orig[1] for orig in orig_avgs]
	return mus, pvals

import string
def bag_of_words(s):
	word_list = s.split()
	bag = {}
	for word in word_list:
		w = word.strip(string.punctuation+string.digits).lower()
		bag[w] = bag.get(w,0) + 1
	try:
		del bag['']
	except:
		pass

	return bag



if __name__ == '__main__':

	if False:
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

	if False:
		for i in range(20):
			print get_business_info(review_data[i][0][Rev.BUS_ID])
			graph_over_time(review_data[i])

	if False:
		for i in range(20):
			print get_business_info(review_data[i][0][Rev.BUS_ID])
			mus, pvals = monte_carlo_months(review_data[i])
			print list_add(mus, -1*mus[0])
			print pvals
			#graph_over_time(review_data[0])

	if True:
		reviews = get_pickled_data('review')
		data = [] #list of label, bag of words: tfs
		term_to_index = {} #term:index
		terms = ['']
		dfs = {} #term:doc freq
		idfs = {} #term:idf
		marked_for_death = []
		for i in range(len(reviews)):
			rev = reviews[i]
			if rev['stars'] == 3:
				marked_for_death.append(i)
				continue
			if rev['stars'] >= 4:
				label = 1
			elif rev['stars'] <= 2: 
				label = -1
			else:
				label = 0
				print 'The shit, weird stars'
			word_bag = bag_of_words(rev['text'])
			reviews[i] = (label, word_bag)
			if i%1000 == 0: print i

		for i in marked_for_death[::-1]:
			del reviews[i]


		ind = 0
		#make idf
		for label, words in reviews:
			ind += 1
			for w in words:
				dfs[w] = idfs.get(w,0) + 1
		num_docs = len(reviews)
		count = 0
		for w in dfs:
			count += 1
			term_to_index[w] = count
			terms.append(w)
			idfs[w] = log(num_docs*1.0/dfs[w])

		count = 0
		text = ''
		for label, word_freqs in reviews:
			ordered = [(term_to_index[w], word_freqs[w]*idfs[w]) for w in word_freqs]
			ordered.sort()
			text = reduce(lambda x,y: x+y, map(lambda x:'%d:%g '%x, ordered), text+'%d '%label) + '\n'
			count += 1
			if count % 1000 == 0: print count

		with open('reviews_vector_norm_large', 'w') as f:
			f.write(text)

		if False:
			count = 0
			with open('reviews_vector_norm_1', 'w') as f:
				print 'writing file'
				for label, word_freqs in reviews[:100000]:
					f.write('%d '%label)
					ordered = [(term_to_index[w], word_freqs[w]*idfs[w]) for w in word_freqs]
					for i in range(len(terms)):
						w = terms[i]
						line = ''
						try:
							line += '%d:%f ' %(i, word_freqs[w]*idfs[w])
						except:
							continue
					f.write(line + '\n')
					count += 1
					if count % 1000 == 0: print count

			with open('reviews_vector_norm_2', 'w') as f:
				print 'writing file'
				for label, word_freqs in reviews[100000:]:
					f.write('%d '%label)
					for i in range(len(terms)):
						w = terms[i]
						f.write(line + '\n')
						try:
							f.write('%d:%f ' %(i, word_freqs[w]*idfs[w]))
						except:
							continue
					count += 1
					if count % 1000 == 0: print count





