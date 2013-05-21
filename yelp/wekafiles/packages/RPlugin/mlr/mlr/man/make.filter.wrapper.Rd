\name{make.filter.wrapper}
\alias{make.filter.wrapper}
\title{Fuse learner with filter method.}
\usage{make.filter.wrapper(learner, id=as.character(NA), label=as.character(NA), vars)
}
\description{Fuses a base learner with a filter method. Creates a learner object, which can be
used like any other learner object. 
Currently only filtering to manually selected variables is supported.}
\value{\code{\linkS4class{learner}}.}
\arguments{\item{learner}{[\code{\linkS4class{learner}} or string]\cr 
Learning algorithm. See \code{\link{learners}}.}
\item{id}{[string] \cr
Id for resulting learner object. If missing, id of "learner" argument is used.}
\item{label}{[string] \cr
Label for resulting learner object. If missing, label of "learner" argument is used.}
\item{vars}{[character]\cr 
Selected variables.}
}
