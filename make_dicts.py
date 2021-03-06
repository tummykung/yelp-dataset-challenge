import sys
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

def make_hash_map(list_of_things, output_filename = ""):
  output = {}

  for i in range(len(list_of_things)):
    output[list_of_things[i]] = i

  if (output_filename != ""):
    with open(output_filename, 'w+') as filename:
      pickle.dump(output, filename)
  return output

def make_reverse_hash_map(hash_map, output_filename = ""):
  output ={hash_map[key]: key for key in hash_map}
    
  if output_filename != "":
    with open(output_filename, 'w+') as filename:
      pickle.dump(output, filename)
      
  return output

def make_user_hashes():
  users = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_user.json')
  list_of_user_ids = []
  for user in users:
    list_of_user_ids += [user["user_id"]]
  user_hash = make_hash_map(list_of_user_ids, 'user_id2num.txt')
  make_reverse_hash_map(user_hash, 'user_num2id.txt')

def make_business_hashes():
  businesses = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json')
  list_of_business_ids = []
  for business in businesses:
    list_of_business_ids += [business["business_id"]]
  business_hash = make_hash_map(list_of_business_ids, 'business_id2num.txt')
  make_reverse_hash_map(business_hash, 'business_num2id.txt')


def make_graph():
  businesses = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json')
  reviews = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json')

  with open('user_id2num.txt', 'r') as filename:
      user_id2num = pickle.load(filename)
  with open('business_id2num.txt', 'r') as filename:
      business_id2num = pickle.load(filename)
      
  output = []
  for i in range(len(reviews)):
    output += [(user_id2num[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]

  
  f = open('temporary.txt', 'w+')
  for edge in output:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()
  return output

if __name__ == '__main__':
  try:
    filename = sys.argv[1]
  except:
    filename = 'yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json'

  #dicts = make_dicts(filename)
  make_graph()
  
