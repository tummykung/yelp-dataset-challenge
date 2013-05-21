\name{make.task}
\alias{make.task}
\title{Construct learning task.}
\usage{make.task(id, label, data, target, excluded, weights, blocking, costs, positive)
}
\description{Defines a learning task for a data set and is the starting point 
for further steps like training, predicting new data, resampling and tuning and benchmarking.
The type (classification or regression) is automatically inferred from the target variable.
It might perform some data conversions in the data.frame, like converting integer input features to numerics, 
but will generally warn about this. 
Whether a classification or regression task is created depends on the data type of the target variable. Factors, logicals or characters
produce classification tasks, numerics produce regression tasks. Integer target variables have to be changed manually.}
\value{\code{\linkS4class{learn.task}}.}
\arguments{\item{id}{[string]\cr 
Id string for object. Used to select the object from a named list, etc.}
\item{label}{[string]\cr 
Label string for object. Used in plots, etc.}
\item{data}{[data.frame] \cr 	
A data frame containing the variables for the modeling.}
\item{target}{[string] \cr
Name of the target variable.}
\item{excluded}{[character]
Names of inputs, which should be generally disregarded, e.g. IDs, etc. Default is zero-length vector.}
\item{weights}{[numeric] \cr 	
An optional vector of weights to be used in the fitting process. Default is not to use weights.}
\item{blocking}{[factor] \cr 	
An optional factor of the same length as the number of observations. Observations with the same blocking level "belong together". Specifically, they are either put all in the training or the test set during a resampling iteration.}
\item{costs}{[matrix] \cr 	
An optional matrix of misclassification costs to be used in the fitting process.
Ignored for regression.}
\item{positive}{[string] \cr 	
Positive class for binary classification. Default is the first factor level of the target attribute. 
Ignored for regression.}
}
