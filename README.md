Yelp Dataset Challenge
======================
um Chaturapruek (Harvey Mudd College), Berit Johnson (Franklin W. Olin College of Engineering)

Mentor: Bálint Daróczy, Számítástechnikai és Automatizálási Kutatóintézet

Abstract: In the first part, we introduce the Matrix Factorization (MF) technique and apply it to the data we got from Yelp Dataset Challenge. MF requires minimal input for predicting user rating with a relatively high accuracy. The results are that, by picking one review per user to form a validation set, we get the Root Mean Square Error (RMSE) of 1.09, whereas we got the RMSE of 1.38 if picking reviews after a specific time to be our validation set (more real-world). We also introduce ideas how this might lead to a next business review recommendation (and how this feature can solve the cold start problem of MF) and possible ethical issues. We try to frame this problem as a game theory problem and then introduce linear programming as a tool to solve it.

In the second part, we perform an analysis on emoticons, particularly smiley faces, with a hypothesis that we can increase the quality of our predictions by incorporating emoticons. We also use the Support Vector Machine (SVM) as a binary classifier. First, we group reviews into binary class: + (4 or 5), - (1 or 2), and discard 3’s as neutral. We create SVM classifier using standard text-mining bag-of-words practices, then attempted to improve it by incorporating emoticons. In the modified SVM, we added a small constant to our confidence if that review contained a smiley face. The Area Under the Curve (AUC) for the original SVM was 0.9513. With smilies, the AUC was 0.9517 =D.



File naming
======================
min5.users - the list of users that have written at least 5 reviews

preprocessing.py - as the name implies, it is a file to convert files into formats compatible with different libraries that we use

/yelp/ - a folder containing SVM code

user_id2num.txt
user_id2num_all.txt
user_num2id.txt
user_num2id_all.txt
user_pickled.txt  - these files are pickled dictionaries or conversion dictionary between user encrypted ID and numerical ID


