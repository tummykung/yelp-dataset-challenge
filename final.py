import sys
import pickle
import random
import time
from datetime import datetime

def make_dicts(filename):
  data = []
  firstList = set()
  L1 = []
  secondList = set()
  L2 = []
  with open(filename) as f:
    for line in f.readlines():
      line = line.strip().split()
      firstList.add(line[0])
      L1.append(line[0])
      secondList.add(line[2])
      L2.append(line[2])
  together = list(firstList.union(secondList))
  output = {}

  for i in range(len(together)):
    output[together[i]] = i + 1 # we use this because graphChi does not like 0
  
  print "writing"
  f = open('myYeast', 'w+')
  for i in range(len(firstList)):
    newline = '%d %d\n' %(output[L1[i]], output[L2[i]])
    f.write(newline)
  f.close()



def add_header_wow(filename):
  data = []
  count = 0
  maxFirst = 0
  maxSecond = 0

  with open(filename) as f:
    old = f.read()
    f.seek(0)
    for line in f.readlines():
      line = line.split()
      firstInt = int(line[0])
      secondInt = int(line[1])
      if(firstInt > maxFirst):
        maxFirst = firstInt

      if(secondInt > maxSecond):
        maxSecond = secondInt

      count += 1

  first_line = "%%MatrixMarket matrix coordinate real general\n% Generated " + time.asctime() + "\n"
  second_line = "%d %d %d\n" %(maxFirst, maxSecond, count) 
  

  with open(filename, 'w') as f:
    f.write(first_line)
    f.write(second_line)
    f.write(old)

def make_hash_map(list_of_things, output_filename = ""):
  output = {}

  for i in range(len(list_of_things)):
    output[list_of_things[i]] = i + 1 # we use this because graphChi does not like 0

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

def make_user_hash_from_review():
  reviews = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json')
  output = set()

  for review in reviews:
    output.add(review["user_id"])

  user_hash = make_hash_map(list(output), "user_id2num_all.txt")
  make_reverse_hash_map(user_hash, 'user_num2id_all.txt')

  return output
  
def make_graph():
  businesses = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json')
  reviews = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json')

  with open('user_id2num_all.txt', 'r') as filename:
      user_id2num_all = pickle.load(filename)
  with open('business_id2num.txt', 'r') as filename:
      business_id2num = pickle.load(filename)
      
  output = []
  for i in range(len(reviews)):
    output += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]

  
  f = open('temporary.txt', 'w+')
  for edge in output:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()
  return output

def make_train_test_and_validation_set():
  #businesses = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json')
  #reviews = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json')

  with open('business_pickled.txt', 'r') as filename:
      businesses = pickle.load(filename)
  with open('review_pickled.txt', 'r') as filename:
       reviews = pickle.load(filename)
  with open('user_id2num_all.txt', 'r') as filename:
      user_id2num_all = pickle.load(filename)
  with open('business_id2num.txt', 'r') as filename:
      business_id2num = pickle.load(filename)
  
  #--------------------------- TEST SET ------------------------------
  print "make a test set"
  test = []
  
  reviews.sort(key = lambda r: r['date'])  

  nUsers = len(reviews)
  userIndices = range(nUsers)
  available = set(userIndices)

  test_index = userIndices[-1001:-1]  

  # OLD COMMENTS: from October 2012, about 30k instances
  available = available.difference(test_index)

  for i in test_index:
    test += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]
  

  #------------------------- VALIDATION SET ----------------------------
  print "make a validation set"
  # METHOD1: randomization
  #train = []
  #percent = 0.66
  #cutoff = int(len(available) * percent)
  #assert(cutoff <= len(available))
  #train_index = random.sample(available, cutoff)
  #validation_index = available.difference(train_index)

  # METHOD2: select the newest ones to be the validation set
  validation_index = userIndices[-20001:-10001]  

  # OLD COMMENTS: from June 2012, about 30k instances
  available = available.difference(validation_index)

  validation = []
  for i in validation_index:
    validation += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]

  #------------------------ TRAINING SET ---------------------------
  print "make a training set"
  train_index = userIndices[0:-20001]

  train = []
  for i in train_index:
    train += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]

  #-------------------------- WRITING --------------------------------
  print "writing"
  f = open('test_graph', 'w+')
  for edge in test:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()


  f = open('test_graph_mm', 'w+')
  for edge in train:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()

  f = open('test_graph_mme', 'w+')
  for edge in validation:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()


def make_train_test_and_validation_set_uniform():
  #businesses = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json')
  #reviews = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json')

  with open('business_pickled.txt', 'r') as filename:
      businesses = pickle.load(filename)
  with open('review_pickled.txt', 'r') as filename:
       reviews = pickle.load(filename)
  with open('user_id2num_all.txt', 'r') as filename:
      user_id2num_all = pickle.load(filename)
  with open('business_id2num.txt', 'r') as filename:
      business_id2num = pickle.load(filename)

  with open('min5.users', 'r') as filename:
      frequent_users_key = []
      for line in filename.readlines():
        frequent_users_key.append(line.strip())
  
  #--------------------------- TEST SET ------------------------------


  print "make a test set"
  test = []
  
  reviews.sort(key = lambda r: r['date'])  
  review2 = filter

  nUsers = len(reviews)
  userIndices = range(nUsers)
  userLastIndices = []

  for i in range(len(frequent_users_key)):
    filtered_users = [review for review in reviews if review["user_id"] == frequent_users_key[i]]
    lastID = user_id2num_all[filtered_users[-1]["user_id"]]
    if(lastID not in userLastIndices):
      userLastIndices.append(lastID)



  available = set(userIndices)

  test_index = random.sample(userLastIndices, 100)
  
  available = available.difference(test_index)

  for i in test_index:
    if (reviews[i]["user_id"] in frequent_users_key):
      test += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]
  

  #------------------------- VALIDATION SET ----------------------------
  print "make a validation set"
  # METHOD1: randomization
  #train = []
  #percent = 0.66
  #cutoff = int(len(available) * percent)
  #assert(cutoff <= len(available))
  #train_index = random.sample(available, cutoff)
  #validation_index = available.difference(train_index)

  # METHOD2: select the newest ones to be the validation set
  validation_index = random.sample(userLastIndices, 1000) 

  # OLD COMMENTS: from June 2012, about 30k instances
  available = available.difference(validation_index)

  validation = []
  for i in validation_index:
    if (reviews[i]["user_id"] in frequent_users_key):
      validation += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]

  #------------------------ TRAINING SET ---------------------------
  print "make a training set"
  train_index = available

  train = []
  for i in train_index:
    if (reviews[i]["user_id"] in frequent_users_key):
      train += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]

  #-------------------------- WRITING --------------------------------
  print "writing"
  f = open('test_graph', 'w+')
  for edge in test:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()


  f = open('test_graph_mm', 'w+')
  for edge in train:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()

  f = open('test_graph_mme', 'w+')
  for edge in validation:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()

def make_train_test_and_validation_set_random():
  #businesses = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_business.json')
  #reviews = make_dicts('yelp_phoenix_academic_dataset/yelp_academic_dataset_review.json')

  with open('business_pickled.txt', 'r') as filename:
      businesses = pickle.load(filename)
  with open('review_pickled.txt', 'r') as filename:
       reviews = pickle.load(filename)
  with open('user_id2num_all.txt', 'r') as filename:
      user_id2num_all = pickle.load(filename)
  with open('business_id2num.txt', 'r') as filename:
      business_id2num = pickle.load(filename)
  
  #--------------------------- TEST SET ------------------------------
  print "make a test set"
  test = []
  
  reviews.sort(key = lambda r: r['date'])  

  nUsers = len(reviews)
  userIndices = range(nUsers)
  available = set(userIndices)

  test_index = random.sample(available, 10000)
  
  # OLD COMMENTS: from October 2012, about 30k instances
  available = available.difference(test_index)

  for i in test_index:
    test += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]
  

  #------------------------- VALIDATION SET ----------------------------
  print "make a validation set"
  # METHOD1: randomization
  #train = []
  #percent = 0.66
  #cutoff = int(len(available) * percent)
  #assert(cutoff <= len(available))
  #train_index = random.sample(available, cutoff)
  #validation_index = available.difference(train_index)

  # METHOD2: select the newest ones to be the validation set
  validation_index = random.sample(available, 10000) 

  # OLD COMMENTS: from June 2012, about 30k instances
  available = available.difference(validation_index)

  validation = []
  for i in validation_index:
    validation += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]

  #------------------------ TRAINING SET ---------------------------
  print "make a training set"
  train_index = available

  train = []
  for i in train_index:
    train += [(user_id2num_all[reviews[i]["user_id"]], business_id2num[reviews[i]["business_id"]], reviews[i]["stars"])]

  #-------------------------- WRITING --------------------------------
  print "writing"
  f = open('test_graph', 'w+')
  for edge in test:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()


  f = open('test_graph_mm', 'w+')
  for edge in train:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()

  f = open('test_graph_mme', 'w+')
  for edge in validation:
    newline = '%d %d %g\n' %edge
    f.write(newline)
  f.close()


  
def add_header(filename):
  data = []
  count = 0
  maxFirst = 0
  maxSecond = 0

  with open(filename) as f:
    old = f.read()
    f.seek(0)
    for line in f.readlines():
      line = line.split()
      firstInt = int(line[0])
      secondInt = int(line[1])
      if(firstInt > maxFirst):
        maxFirst = firstInt

      if(secondInt > maxSecond):
        maxSecond = secondInt

      count += 1

  first_line = "%%MatrixMarket matrix coordinate real general\n% Generated " + time.asctime() + "\n"
  second_line = "%d %d %d\n" %(maxFirst, maxSecond, count) 
  

  with open(filename, 'w') as f:
    f.write(first_line)
    f.write(second_line)
    f.write(old)

def dictionary_to_weka():
  with open('user_id2num_all.txt', 'r') as filename:
    user_id2num_all = pickle.load(filename)
  with open('business_id2num.txt', 'r') as filename:
    business_id2num = pickle.load(filename)
  with open('business_pickled.txt', 'r') as filename:
    businesses = pickle.load(filename)
  with open('review_pickled.txt', 'r') as filename:
    reviews = pickle.load(filename)

  output = []
  for i in range(10):
    output += [(business_id2num[reviews[i]["business_id"]],
      reviews[i]["date"],
      reviews[i]["stars"])]

  with open('test.csv', 'w+') as f:
    for edge in output:
      newline = '%d, %s, %d\n' %edge
      f.write(newline)
  
if __name__ == '__main__':
  try:
    filename = sys.argv[1]
  except:
    filename = 'yeast-high-fidelity-interactome.sif'

  make_dicts(filename)
  #make_train_test_and_validation_set_uniform()
  #make_train_test_and_validation_set()
  #add_header("test_graph")
  #add_header("test_graph_mm")
  #add_header("test_graph_mme")
  #dicts = make_dicts(filename)
  #make_user_hashes()
  #make_business_hashes()
  #make_graph()
  #make_user_hash_from_review()
  #make_train_and_validation()
  #dictionary_to_weka()
