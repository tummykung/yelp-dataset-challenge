\name{tune}
\alias{tune}
\title{Hyperparameter tuning}
\usage{tune(learner, task, resampling, control, measures, aggr, model=FALSE,
    path=FALSE)
}
\description{Optimizes the hyperparameters of a learner for a classification or regression problem.
Allows for different optimization methods, commonly grid search is used but other search techniques
are available as well.
The specific details of the search algorithm are set by passing a control object.}
\details{The first measure, aggregated by the first aggregation function is optimized, to find a set of optimal hyperparameters.}
\value{\code{\linkS4class{opt.result}}.}
\seealso{\code{\link{grid.control}}, \code{\link{optim.control}}, \code{\link{cmaes.control}}}
\arguments{\item{learner}{[\code{\linkS4class{learner}} or string]\cr 
Learning algorithm. See \code{\link{learners}}.}
\item{task}{[\code{\linkS4class{learn.task}}] \cr
Learning task.}
\item{resampling}{[\code{\linkS4class{resample.instance}}] or [\code{\linkS4class{resample.desc}}]\cr
Resampling strategy to evaluate points in hyperparameter space. At least for grid search, if you pass a description, 
it is instantiated at one, so all points are evaluated on the same training/test sets.}
\item{control}{[\code{\linkS4class{tune.control}}] \cr
Control object for search method. Also selects the optimization algorithm for tuning.}
\item{measures}{[see \code{\link{measures}}]\cr
Performance measures.}
\item{aggr}{[see \code{\link{aggregations}}]\cr
Aggregation functions.}
\item{model}{[boolean]\cr
Should a final model be fitted on the complete data with the best found hyperparameters? Default is FALSE.}
\item{path}{[boolean]\cr
Should optimization path be saved? Default is FALSE.}
}
