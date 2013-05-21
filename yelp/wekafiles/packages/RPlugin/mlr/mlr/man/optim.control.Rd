\name{optim.control}
\alias{optim.control-class}
\alias{optim.control}
\title{optim.control-class}
\section{Extends}{\code{\linkS4class{tune.control}}}
\alias{optim.control-methods}
\alias{optim.control}
\description{Control structure for tuning with optim (Nelder-Mead, SANN, etc).}
\value{\code{optim.control-methods}: Control structure for tuning.

}
\arguments{\item{minimize}{[logical] \cr 
Minimize performance measure? Default is TRUE.}
\item{tune.threshold}{[logical] \cr 
Perform empirical thresholding? Default is FALSE. Only supported for binary classification and you have to set predict.type to "prob" for this in make.learner.}
\item{thresholds}{[numeric] \cr 
Number of thresholds to try in tuning. Predicted probabilities are sorted and divided into groups of equal size. Default is 10.}
\item{path}{[boolean]\cr
Should optimization path be saved?}
\item{start}{[numeric] \cr
Named vector of initial values.}
\item{lower}{[numeric] \cr
Named vector of lower boundary constraints. Default is -Inf. Only used in "L-BFGS-B".}
\item{upper}{[numeric] \cr
Named vector of upper boundary constraints. Default is Inf. Only used in "L-BFGS-B".}
\item{scale}{[\code{\link{function}}] \cr 
A function to scale the hyperparameters. E.g. maybe you want to optimize in some log-space.
Has to take a vector and return a scaled one. Default is identity function.}
\item{...}{Further control parameters passed to the \code{control} argument of \code{\link[stats]{optim}}.}
}
\alias{optim.control,logical,logical,integer,logical,numeric,numeric,numeric,function-method}
\alias{optim.control}
