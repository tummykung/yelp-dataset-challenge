TR=reviews.lsvm
TE=reviews.lsvm

NAME=all

MOD=mod.$NAME
PRED=pred.$NAME
RES=res.$NAME

time ./svm-train -b 1 -s 0 -t 0 -m 5000 $TR $MOD 
time ./svm-predict -b 1 $TE $MOD $PRED

awk '{if(NR==1) {n=2; if($2==-1) n=3; } else print $n;}' $PRED > $RES

paste -d' ' $RES $TE | cut -d' ' -f1,2 | awk -f roc.awk 
