import pickle

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

businesses = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json')
reviews = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json')

with open('user_id2num_all.txt', 'r') as filename:
  user_id2num_all = pickle.load(filename)
with open('business_id2num.txt', 'r') as filename:
  business_id2num = pickle.load(filename)