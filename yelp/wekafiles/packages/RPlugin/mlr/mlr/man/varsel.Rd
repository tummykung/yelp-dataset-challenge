\name{varsel}
\alias{varsel}
\title{Variable selection.}
\usage{varsel(learner, task, resampling, control, measures, aggr, model=FALSE,
    path=FALSE)
}
\description{Optimizes the variables for a classification or regression problem by choosing a variable selection wrapper approach.
Allows for different optimization methods.
The specific details of the search algorithm are set by passing a control object.
Currently you can use \code{\link{sequential.control}} and \code{\link{randomvarsel.control}}. 
The first measure, aggregated by the first aggregation function is optimized, to find a set of optimal variables.}
\value{\code{\linkS4class{opt.result}}.}
\seealso{\code{\link{varsel.control}}, \code{\link{make.varsel.wrapper}}}
\arguments{\item{learner}{[\code{\linkS4class{learner}} or string]\cr 
Learning algorithm. See \code{\link{learners}}.}
\item{task}{[\code{\linkS4class{learn.task}}] \cr
Learning task.}
\item{resampling}{[\code{\linkS4class{resample.instance}}] or [\code{\linkS4class{resample.desc}}]\cr
Resampling strategy to evaluate points in hyperparameter space.}
\item{control}{[see \code{\link{varsel.control}}]
Control object for search method. Also selects the optimization algorithm for feature selection.}
\item{measures}{[see \code{\link{measures}}]\cr
Performance measures.}
\item{aggr}{[see \code{\link{aggregations}}]\cr
Aggregation functions.}
\item{model}{[boolean]\cr
Should a final model be fitted on the complete data with the best found features? Default is FALSE.}
\item{path}{[boolean]\cr
Should optimization path be saved? Default is FALSE.}
}
