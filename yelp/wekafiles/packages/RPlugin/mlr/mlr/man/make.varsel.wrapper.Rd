\name{make.varsel.wrapper}
\alias{make.varsel.wrapper}
\title{Fuse learner with variable selection.}
\usage{make.varsel.wrapper(learner, id=as.character(NA), label=as.character(NA), resampling,
    measures, aggr, control)
}
\description{Fuses a base learner with a search strategy to select variables. Creates a learner object, which can be
used like any other learner object, but which internally uses varsel. If the train function is called on it, the search strategy and resampling are invoked
to select an optimal set variables. Finally, a model is fitted on the complete training data with these variables and returned.}
\value{\code{\linkS4class{learner}}.}
\seealso{\code{\link{varsel}}, \code{\link{varsel.control}}}
\arguments{\item{learner}{[\code{\linkS4class{learner}} or string]\cr 
Learning algorithm. See \code{\link{learners}}.}
\item{id}{[string] \cr
Id for resulting learner object. If missing, id of "learner" argument is used.}
\item{label}{[string] \cr
Label for resulting learner object. If missing, label of "learner" argument is used.}
\item{resampling}{[\code{\linkS4class{resample.instance}}] or [\code{\linkS4class{resample.desc}}]\cr
Resampling strategy to evaluate points in hyperparameter space.}
\item{control}{Control object for search method. Also selects the optimization algorithm for feature selection.}
\item{measures}{[see \code{\link{measures}}]
Performance measures.}
\item{aggr}{[see \code{\link{aggregations}}]
Aggregation functions.}
}
