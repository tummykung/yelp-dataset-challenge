\name{sequential.control}
\alias{sequential.control-class}
\alias{sequential.control}
\title{sequential.control-class}
\section{Extends}{\code{\linkS4class{varsel.control}}}
\section{Methods}{\describe{}}
\alias{sequential.control-methods}
\alias{sequential.control}
\description{Control structure for sequential variable selection.}
\value{\code{sequential.control-methods}: Control structure.

}
\arguments{\item{minimize}{[logical] \cr 
Minimize performance measure? Default is TRUE.}
\item{tune.threshold}{[logical] \cr 
Perform empirical thresholding? Default is FALSE. Only supported for binary classification and you have to set predict.type to "prob" for this in make.learner.}
\item{thresholds}{[numeric] \cr 
Number of thresholds to try in tuning. Predicted probabilities are sorted and divided into groups of equal size. Default is 10.}
\item{path}{[boolean]\cr
Should optimization path be saved?}
\item{max.vars}{[integer] \cr 
Maximal number of allowed variables in the final set. Default is max. integer.}
\item{method}{[\code{\link{character}}] \cr
Search method. Currently supported are sequential forward search "sfs", sequential backward search "sbs", 
sequential floating forward search "sffs", sequential floating backward search "sfbs". Default is "sfs".}
\item{alpha}{[numeric] \cr 
sfs, sffs: In a forward step, minimal improvement of performance measure. Can be negative.}
\item{beta}{[numeric] \cr 
sbs, sfbs: In a backward step, minimal improvement of performance measure. Can be negative.}
}
\alias{sequential.control,logical,logical,integer,logical,integer,character,numeric,numeric-method}
\alias{sequential.control}
