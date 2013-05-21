\name{parallel.setup}
\alias{parallel.setup}
\title{Parallelization setup.}
\usage{parallel.setup(mode="local", parallel.type, cpus, level="resample", ...)
}
\description{Defines the technical mode and level of parallelization when executing code.}
\details{Note that when you parallelize a function, e.g. \code{\link{resample.fit}}, this also works when the function is internally called by 
\code{mlr} in a more complex function, e.g. \code{\link{bench.exp}}.}
\arguments{\item{mode}{[string] \cr
Which parallel mode should be used: 
"local", "multicore", "snowfall", "sfCluster".
Default is "local" without parallel execution.}
\item{parallel.type}{[string] \cr
Currently this is only used for \code{\link[snowfall]{sfInit}} (mode="snowfall"), where it is passed to the "type" argument. 
Default is "MPI", meaning we use "Rmpi".}
\item{cpus}{[numeric] \cr
Number of requested cpus. Default is \code{\link[Rmpi]{mpi.universe.size}}() for snowfall/MPI, ignored for for sfCluster and 1 otherwise.}
\item{level}{[string] \cr
What is parallelized / what is a job. 
"resample": \code{\link{resample.fit}} is parallelized and a job is train / test.
"tune": \code{\link{tune}} is parallelized and a job is a resampled evaluation of one hyperparameter setting.  
"varsel": \code{\link{varsel}} is parallelized and a job is a resampled evaluation of a feature set.
"bench": \code{\link{bench.exp}} is parallelized and a job is completely evaluating one learner on one data set.}
\item{...}{[any] \cr
Optional parameters, only passed to \code{\link[snowfall]{sfInit}} currently.}
}
