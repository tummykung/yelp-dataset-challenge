\name{grid.control}
\alias{grid.control-class}
\alias{grid.control}
\title{grid.control-class}
\section{Extends}{\code{\linkS4class{tune.control}}}
\alias{grid.control-methods}
\alias{grid.control}
\description{Control structure for grid search tuning.}
\value{\code{grid.control-methods}: Control structure for tuning.

}
\arguments{\item{minimize}{[logical] \cr 
Minimize performance measure? Default is TRUE.}
\item{tune.threshold}{[logical] \cr 
Perform empirical thresholding? Default is FALSE. Only supported for binary classification and you have to set predict.type to "prob" for this in make.learner.}
\item{thresholds}{[numeric] \cr 
Number of thresholds to try in tuning. Predicted probabilities are sorted and divided into groups of equal size. Default is 10.}
\item{path}{[boolean]\cr
Should optimization path be saved?}
\item{ranges}{[\code{\link{list}}] \cr 
A list of named vectors/lists of possible values for each hyperparameter. 
You can also pass a list of such ranges by using [\code{\link{combine.ranges}}] 
in the rare case when it does not make sense to search a complete cross-product of range values.}
\item{scale}{[\code{\link{function}}] \cr 
A function to scale the hyperparameters. E.g. maybe you want to optimize in some log-space.
Has to take a vector and return a scaled one. Default is identity function.}
}
\alias{grid.control,logical,logical,integer,logical,list,function-method}
\alias{grid.control}
