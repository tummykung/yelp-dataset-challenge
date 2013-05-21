\name{bench.exp}
\alias{bench.exp}
\title{Benchmark experiment for multiple learners and tasks.}
\usage{bench.exp(learners, tasks, resampling, measures, conf.mats=TRUE, predictions=FALSE, models=FALSE, paths=FALSE)}
\description{Complete benchmark experiment to compare different learning algorithms 
across one or more tasks w.r.t. a given resampling strategy.  
Experiments are paired, meaning always the same training / test sets are used for the different learners.}
\value{\code{\linkS4class{bench.result}}.}
\note{You can also get automatic, internal tuning by using \code{\link{make.tune.wrapper}} with your learner.}
\seealso{\code{\link{make.tune.wrapper}}}
\alias{bench.exp}
\arguments{\item{learners}{[string | \code{\linkS4class{learner}} | list of the previous two] \cr
Defines the learning algorithms which should be compared.}
\item{tasks}{[\code{\link{learn.task}} | list of the previous] \cr
Defines the tasks.}
\item{resampling}{[resampling desc | resampling instance | list of the previous two] \cr
Defines the resampling strategies for the tasks.}
\item{measures}{[see \code{\link{measures}}]
Performance measures.}
\item{conf.mats}{[logical] \cr
Should confusion matrices be stored?
Default is TRUE.
Ignored for regression.}
\item{predictions}{[logical] \cr
Should all predictions be stored?
Default is FALSE.}
\item{models}{[logical] \cr
Should all fitted models be stored?
Default is FALSE.}
\item{paths}{[logical] \cr
Should the optimization paths be stored?
Default is FALSE.}
}
