\name{cmaes.control}
\alias{cmaes.control-class}
\alias{cmaes.control}
\title{cmaes.control-class}
\section{Extends}{\code{\linkS4class{tune.control}}}
\alias{cmaes.control-methods}
\alias{cmaes.control}
\description{Control structure for CMA-ES tuning.}
\value{\code{cmaes.control-methods}: Control structure for tuning.

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
Named vector of lower boundary constraints. Default is -Inf.}
\item{upper}{[numeric] \cr
Named vector of upper boundary constraints. Default is Inf.}
\item{scale}{[\code{\link{function}}] \cr 
A function to scale the hyperparameters. E.g. maybe you want to optimize in some log-space.
Has to take a vector and return a scaled one. Default is identity function.}
\item{...}{Further control parameters passed to the \code{control} argument of \code{\link[cmaes]{cma_es}}.}
}
\alias{cmaes.control,logical,logical,integer,logical,numeric,numeric,numeric,function-method}
\alias{cmaes.control}
