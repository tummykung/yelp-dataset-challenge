# For details please visit Danny Bickson's blog:
# http://bickson.blogspot.hu/2012/12/collaborative-filtering-with-graphchi.html.
# The source for ALS, SGD and other MF algorithms: /home/daroczyb/graphchi/src/graphchi/toolkits/collaborative_filtering

TRAINING='test_graph' # training set
K=100        # dimension of latent feature space
MAXITER=100 # maximum number of iterations
THREADS=25 # number of threads



# echo "Alternate least square (ALS) on "$TRAINING
#./als --matrixmarket=true --training=$TRAINING --D=$K --max_iter=$MAXITER --ncpus=$THREADS --quiet=1 --lambda=0.01
#echo "Stochastic Gradient Descent (SGD) on "$TRAINING
#./sgd --matrixmarket=true --training=$TRAINING --D=$K --max_iter=$MAXITER --ncpus=$THREADS --quiet=1 --lambda=0.01

TEST='test_graph'
TRAINING='test_graph_mm' # training set
VALIDATION='test_graph_mme' # validation set
K=100

#echo "(lambda = 0.01) Alternate least square (ALS) on "$TRAINING 
#./als --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.001

#echo "(lambda = 0.1) Alternate least square (ALS) on "$TRAINING 
#./als --matrixmarket=true  --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.01

#echo "(lambda = 1) Alternate least square (ALS) on "$TRAINING 
#./als --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1


#echo "Alternate least square (ALS) on "$TRAINING
#./als --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1

#echo "Stochastic Gradient Descent (SGD) on "$TRAINING
#./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1

K=100
echo "0 "$TRAINING
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1

K=50
echo "1 "$TRAINING
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1

K=50
echo "2"$TRAINING
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.01


K=13
echo "3"$TRAINING
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=-5 --maxval=5 --quiet=1 --lambda=0.3


K=13
echo "4"$TRAINING
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=-5 --maxval=5 --quiet=1 --lambda=0.5

#K=200
#echo "K = 200; Stochastic Gradient Descent (SGD) on "$TRAINING
#./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1

#echo "lambda = 0.01; Stochastic Gradient Descent (SGD) on "$TRAINING
#./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.01


#./sparse_als --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1

#./svd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1

