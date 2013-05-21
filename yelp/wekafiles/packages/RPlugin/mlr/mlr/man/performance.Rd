\name{performance}
\alias{performance,grouped.prediction,list,list,list-method}
\alias{performance}
\title{performance,grouped.prediction,list,list,list-method}
\alias{performance-methods}
\alias{performance}
\usage{performance(pred, measures, aggr, losses, task)}
\description{Measures the quality of predictions w.r.t. some performance measures or loss functions.}
\value{\code{performance-methods}: A list with with possibly three named components: "measures" is a data.frame of performance values,
"aggr" a data.frame of aggregated values, "losses" a data.frame of losses.

}
\arguments{\item{pred}{[\code{\linkS4class{prediction}}] \cr
Prediction object to evaluate.}
\item{measures}{[see \code{\link{measures}}]
Performance measures.}
\item{aggr}{[see \code{\link{aggregations}}]
Aggregation functions. 
Ignored if not a \code{\linkS4class{resample.prediction}}}
\item{losses}{[see \code{\link{losses}}]
Loss functions.}
\item{task}{[\code{\linkS4class{learn.task}}]\cr 
Optionally specifies learning task, very rarely needed.}
}
\alias{performance,prediction,list,list,list-method}
\alias{performance}
\alias{performance,resample.prediction,list,list,list-method}
\alias{performance}
