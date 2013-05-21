#
# $1 a josolt ertek (az "+1" osztaly valoszinusege, bar nem kotelezo 0-1 kozott lennie, csak a asorrend szamit)
# $2 az igazi class
#
# ! figyelem itt trapez-szaballyal van a gorbe alatti terulet, szoval pl a konstans 0 joslat az 0.5-os erteket ad es nem 0-t !
#
# pelda:
#
# cat proba_adat.txt | awk -v file=tmp_roc_curve  -f ./eval/roc.awk
#
# ha a roc goret is ki akarjuk iratni az a file parameterrel lehet:
# cat proba_adat.txt | awk -v file=tmp_roc_curve  -f ./eval/roc.awk
#
# es akkor meg lehet nezni a roc gorrbet:
# gnuplot <(echo "plot 'tmp_roc_curve'; pause -1")
#

BEGIN{
  count=0;
}

{
  class[NR]=$2;
  value[NR]=1-$1;

#  value[NR]=$2;
  ++count;
}

END{

  v_count=0;
  for(i=1;i<=count;++i){
    ++v_num[ value[i] ];
    if( !(value[i] in v) ){
      v[ value[i] ] = 1;
      all_v[ v_count ] = value[i];
      ++v_count;
    }
  }

  asort(all_v);
  for(i=1;i<=v_count;++i){
    v_ind[ all_v[i] ] = i;
    p[i]=0;
    n[i]=0;
  }

  all_p=0;
  all_n=0;
  for(i=1;i<=count;++i){
    ind = v_ind[ value[i] ];
    if(1==class[i]+0){
      ++p[ind];
      ++all_p;
    } else {
      ++n[ind];
      ++all_n;
    }
  }

  sum_p=0;
  sum_n=0;
  sum=0;
  x1=0;
  y1=0;
  t=0;

  if(""!=file){
    print 0,0 > file;
  }
  for(i=1;i<=v_count;++i){
    sum_p+=p[i];
    sum_n+=n[i];
    sum=sum_p+sum_n;
    if(0!=all_n){
      x2=sum_n/all_n;
    } else {
      x2=0;
    }
    if(0!=all_p){
      y2=sum_p/all_p;
    } else {
      y2=0;
    }
    t+=(x2-x1)*(y1+y2)/2;
    x1=x2;
    y1=y2;
    if(""!=file){
      print x1,y1 > file;
    }
  }

  print t;

#  print count, all_p, all_n, sum_p, sum_n > file;

}
