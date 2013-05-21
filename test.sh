# For details please visit Danny Bickson's blog:
# http://bickson.blogspot.hu/2012/12/collaborative-filtering-with-graphchi.html.
# The source for ALS, SGD and other MF algorithms: /home/daroczyb/graphchi/src/graphchi/toolkits/collaborative_filtering

TRAINING='test_graph' # training set
K=10        # dimension of latent feature space
MAXITER=100 # maximum number of iterations
THREADS=16 # number of threads



# echo "Alternate least square (ALS) on "$TRAINING
#./als --matrixmarket=true --training=$TRAINING --D=$K --maxiter=$MAXITER --ncpus=$THREADS --quiet=1 --lambda=0.01
#echo "Stochastic Gradient Descent (SGD) on "$TRAINING
#./sgd --matrixmarket=true --training=$TRAINING --D=$K --maxiter=$MAXITER --ncpus=$THREADS --quiet=1 --lambda=0.01

TRAINING='test_graph_mm' # training set
VALIDATION='test_graph_mme' # validation set
K=100

echo "Alternate least square (ALS) on "$TRAINING
./als --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --maxiter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1
#echo "Stochastic Gradient Descent (SGD) on "$TRAINING
#./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --maxiter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1


