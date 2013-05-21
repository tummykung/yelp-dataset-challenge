\name{ROCR.plot.task}
\alias{ROCR.plot.task}
\title{Compare ROC curves of learners in a benchmark experiment.}
\usage{ROCR.plot.task(x, task.id, learner.ids=x["learners"], perf1="tpr", perf2="fpr",
    legend.x="bottomright", legend.y, col, ...)
}
\description{Compare ROC curves of learners in a benchmark experiment.}
\seealso{\code{\link{ROCR.plot.performance}}}
\arguments{\item{x}{[\code{\linkS4class{bench.result}}] \cr
Benchmark result. Must contain predictions.}
\item{task.id}{[string] \cr
Id of a task from \code{x}.}
\item{learner.ids}{[character] \cr
Ids of learners to plot. Default are all learners in \code{x} for selected task.}
\item{perf1}{[string] \cr
First ROC measure for y-axis. Note that this must be a ROCR measure, see \code{\link{ROCR.performance}}. 
Default is "tpr".}
\item{perf2}{[string] \cr
Second ROC measure for x-axis. Note that this must be a ROCR measure, see \code{\link{ROCR.performance}}. 
Default is "fpr".}
\item{legend.x}{[any] \cr 
Where should legend be placed. Any placement accepted by \code{\link{legend}} is ok.
"none" does not plot a legend.}
\item{legend.y}{[any] \cr 
Where should legend be placed. Any placement accepted by \code{\link{legend}} is ok.}
\item{col}{[character] \cr 
Colors for ROC curves. Default are rainbow colors.}
\item{...}{[any] \cr 
Further arguments that are passed to \code{\link{ROCR.plot.performance}}.}
}
