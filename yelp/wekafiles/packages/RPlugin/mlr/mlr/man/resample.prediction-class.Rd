\name{resample.prediction-class}
\alias{as.list,grouped.prediction-method}
\alias{as.list}
\title{Converts object to a list of lists of normal prediction objects - one for each iteration and group...}
\description{Converts object to a list of lists of normal prediction objects - one for each iteration and group}
\alias{resample.prediction-class}
\alias{resample.prediction}
\section{Extends}{\code{\linkS4class{prediction}}}
\section{Methods}{\describe{\item{\code{\link[=as.ROCR.preds,resample.prediction-method]{as.ROCR.preds}}}{}\item{\code{\link[=performance,resample.prediction,list,list,list-method]{performance}}}{}\item{\code{\link[=to.string,resample.prediction-method]{to.string}}}{}\item{\code{\link[=[,resample.prediction-method]{[}}}{\code{signature( = "resample.prediction")}: Getter}\item{\code{\link[=as.list,resample.prediction-method]{as.list}}}{\code{signature( = "resample.prediction")}: Converts object to a list of normal prediction objects - one for each iteration.}}}
\alias{[,resample.prediction-method}

\details{\code{[,resample.prediction-method}: Note that in the case of the "prob", "response" and "decision"
fields, the results are returned in the order they were used by
the resampling strategy and not in the order present in the
dataset. This mainly applies to cross-validation were a different
order might be expected.

}
\alias{as.list,resample.prediction-method}
\alias{as.list}
