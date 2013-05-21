# For details please visit Danny Bickson's blog:
# http://bickson.blogspot.hu/2012/12/collaborative-filtering-with-graphchi.html.
# The source for ALS, SGD and other MF algorithms: /home/daroczyb/graphchi/src/graphchi/toolkits/collaborative_filtering

TRAINING='test_graph' # training set
MAXITER=60 # maximum number of iterations
THREADS=16 # number of threads
TEST='test_graph'
TRAINING='test_graph_mm' # training set
VALIDATION='test_graph_mme' # validation set


K=100
echo "0.01 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.01
echo "0.1 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1
echo "1 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=-5 --maxval=5 --quiet=1 --lambda=1

K=13
echo "0.01 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.01
echo "0.1 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1
echo "1 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=-5 --maxval=5 --quiet=1 --lambda=1


K=5
echo "0.01 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.01
echo "0.1 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=1 --maxval=5 --quiet=1 --lambda=0.1
echo "1 - Stochastic Gradient Descent (SGD) on "$TRAINING"with K ="$K
./sgd --matrixmarket=true --training=$TRAINING --validation=$VALIDATION --D=$K --max_iter=$MAXITER --ncpus=$THREADS --minval=-5 --maxval=5 --quiet=1 --lambda=1

