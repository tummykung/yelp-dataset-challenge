\name{opt.result-class}
\alias{opt.result-class}
\alias{opt.result}
\title{Optimization result.}
\description{Container for results of hyperparameter tuning or variable selection.
Contains the obtained optimal parameter vector, its performance values
and the optimization path which lead there. It might also optionally
contain a wrapped.model, which was fitted by using the optimal
parameters.}
\details{\code{opt.result-class}: Getter.\cr

\describe{
\item{opt.type [string]}{Currently 'tune' or 'varsel'.}
\item{par [list | character]}{Named list of hyperparameter values or character vector of variables, identified as optimal.}
\item{tuned.par [list]}{If tuning was performed, best found set of hyperparameters.}
\item{sel.vars [character]}{If variable selection was performed, best found set of variables.}
\item{perf [numeric]}{Performance values of 'par'.}
\item{path [list | data.frame]. Optional parameters: as.data.frame}{Optimization path. Can be converted to a data.frame if as.data.frame is TRUE.}
\item{model [\code{\linkS4class{wrapped.model}}]}{Model fitted with settings in 'par'. Will be NULL, if fitting was not requested.}
\item{learner [\code{\linkS4class{wrapped.model}}]}{Learner with settings in 'par'. Currently only supported for hyperparameter tuning.}
}

}
\seealso{\code{\link{tune}}, \code{\link{varsel}}}
\section{Extends}{\code{\linkS4class{object}}}
\section{Methods}{\describe{\item{\code{\link[=[,opt.result-method]{[}}}{}\item{\code{\link[=to.string,opt.result-method]{to.string}}}{}}}
\alias{[,opt.result-method}

