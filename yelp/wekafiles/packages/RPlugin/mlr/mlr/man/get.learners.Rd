\name{get.learners}
\alias{get.learners-methods}
\alias{get.learners}
\title{Find matching learning algorithms.}
\description{Returns the names of learning algorithms which have specific characteristics, e.g.
whether it supports missing values, misclassification costs, case weights,...
or which are are able to solve a given \code{\linkS4class{learn.task}}.}
\details{\code{get.learners-methods}: The default of all boolean parameters is NA, meaning: property is not required, don't care.

}
\arguments{\item{x}{[string | \code{\linkS4class{learn.task}}] \cr
Type of the learning algorithm, either "classif" or "regr" or task to solve}
\item{numerics}{[boolean] \cr
Supports numeric inputs? Pass only when x is a string.}
\item{factors}{[boolean] \cr
Supports factor inputs? Pass only when x is a string.}
\item{characters}{[boolean] \cr
Supports character inputs? Pass only when x is a string.}
\item{missings}{[boolean] \cr
Supports missing values? Pass only when x is a string.}
\item{multiclass}{[boolean] \cr
Supports multiclass problems? Pass only when x is a string.}
\item{weights}{[boolean] \cr
Supports case weights? Pass only when x is a string.}
\item{probs}{[boolean] \cr
Can predict probabilities?}
\item{decision}{[boolean] \cr
Supports decision values?}
\item{costs}{[boolean] \cr
Supports non-standard misclassification costs?}
}
\alias{get.learners,character-method}
\alias{get.learners}
\alias{get.learners,learn.task-method}
\alias{get.learners}
