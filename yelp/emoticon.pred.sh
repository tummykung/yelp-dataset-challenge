grep $1 reviews.te.noneutral | awk '{print $7; }' > pred.te.smiley

awk 'BEGIN{while(getline < "pred.te.smiley") v[$1]=1;}{if(v[$7]==1) print 1,$3,$7; else print 0,$3,$7;}' reviews.te.noneutral | tr ':' ' ' > pred.smiley.te

awk -v B=$2 'BEGIN{if(B==1) {n=1; n2=0;} else {n=0; n2=1;}}{if($2==5 || $2==4) print $1,n; else print $1,n2;}' pred.smiley.te | awk -f roc.awk

paste -d' ' res.trte.anno pred.smiley.te | awk '{print int($3*100000000)/100000000,$1,$2,$4;}' | awk -v N=$3 '{n=1; if($3==-1) n=0; print N*$1+$2,n;}' > pred.comb.$3

awk -f roc.awk < pred.comb.$3
