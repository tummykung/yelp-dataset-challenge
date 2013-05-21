\name{tune.threshold}
\alias{tune.threshold}
\title{Tune prediction threshold.}
\usage{tune.threshold(pred, measures, aggr, task, minimize=TRUE, thresholds=10)
}
\description{Optimizes the threshold of prediction based on probabilities or decision values.
Currently only implemented for probabilities and binary classification.}
\value{A list with with the following components: "th" is the optimal threshold, pred a prediction object based on "th", 
"th.seq" a numerical vector of threhold values which were tried and "perf" their respective performance values.}
\seealso{\code{\link{tune}}}
\arguments{\item{pred}{[\code{\linkS4class{prediction}}] \cr
Prediction object to use for tuning the treshold.}
\item{measures}{[see \code{\link{measures}}]
Performance measures.}
\item{aggr}{[see \code{\link{aggregations}}]
Aggregation functions. 
Ignored if not a \code{\linkS4class{resample.prediction}}}
\item{task}{[\code{\linkS4class{learn.task}}] \cr
Learning task. Rarely neeeded, only when required for the performance measure.}
\item{minimize}{[logical] \cr 
Minimize performance measure? Default is TRUE.}
\item{thresholds}{[integer] \cr
Number of thresholds to try in tuning.}
}
