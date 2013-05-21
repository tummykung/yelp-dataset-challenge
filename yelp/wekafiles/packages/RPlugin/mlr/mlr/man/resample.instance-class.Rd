\name{resample.instance-class}
\alias{resample.instance-class}
\alias{resample.instance}
\title{Resampling instance.}
\description{Base class for specific resampling draws like cross-validation or bootstrapping.
This class encapsulates training and test sets generated from the data set for a number of iterations. 
It mainly stores a set of integer vectors indicating the training examples for each iteration.
Don't create objects from this class directly but use the corresponding subclasses.
For construction simply use the factory method \code{\link{make.res.instance}}.}
\details{\code{resample.instance-class}: Getter.

\describe{
\item{size [integer]}{Number of observations.}
\item{name [character]}{The name of the resample description object, i.e. the type of resampling.}
\item{iters [integer]}{The number of resampling iterations.}
\item{train.inds [list | integer] Optional parameter: j}{If j is a single integer, the vector of training indices for the jth iteration. Otherwise, the list of indices for iterations. Missing j means list of all indices.}
\item{test.inds [list | integer] Optional parameter: j}{If j is a single integer, the vector of test indices for the jth iteration. Otherwise, the list of indices for iterations j. Missing j means list of all indices.}
}

}
\note{If you want to add another resampling strategy, have a look at the web documentation.}
\seealso{\code{\linkS4class{resample.desc}}, \code{\link{make.res.instance}}, \code{\link{resample.fit}}}
\section{Extends}{\code{\linkS4class{object}}}
\section{Methods}{\describe{\item{\code{\link[=get.train.set,resample.instance,integer-method]{get.train.set}}}{}\item{\code{\link[=get.test.set,resample.instance,integer-method]{get.test.set}}}{}\item{\code{\link[=resample.update,resample.instance,learn.task,wrapped.model,prediction-method]{resample.update}}}{}\item{\code{\link[=resample.done,resample.instance-method]{resample.done}}}{}\item{\code{\link[=resample.fit,learner,learn.task,resample.instance,list,character,function-method]{resample.fit}}}{}\item{\code{\link[=[,resample.instance-method]{[}}}{}\item{\code{\link[=to.string,resample.instance-method]{to.string}}}{}}}
\alias{[,resample.instance-method}

