\name{randomvarsel.control}
\alias{randomvarsel.control-class}
\alias{randomvarsel.control}
\title{randomvarsel.control-class}
\section{Extends}{\code{\linkS4class{varsel.control}}}
\section{Methods}{\describe{}}
\alias{randomvarsel.control-methods}
\alias{randomvarsel.control}
\description{Control structure for random variable selection.}
\value{\code{randomvarsel.control-methods}: Control structure.

}
\arguments{\item{minimize}{[logical] \cr 
Minimize performance measure? Default is TRUE.}
\item{tune.threshold}{[logical] \cr 
Perform empirical thresholding? Default is FALSE. Only supported for binary classification and you have to set predict.type to "prob" for this in make.learner.}
\item{thresholds}{[numeric] \cr 
Number of thresholds to try in tuning. Predicted probabilities are sorted and divided into groups of equal size. Default is 10.}
\item{path}{[boolean]\cr
Should optimization path be saved?}
\item{maxit}{[integer] \cr 
Maximal number of variable sets to evaluate. Default is 100.}
\item{method}{[numeric] \cr 
Currently only "binomial" is implemented. Samples variables from a binomial distribution.}
\item{prob}{[numeric] \cr 
Parameter for binomial distribution.}
}
\alias{randomvarsel.control,logical,logical,integer,logical,integer,character,numeric-method}
\alias{randomvarsel.control}
