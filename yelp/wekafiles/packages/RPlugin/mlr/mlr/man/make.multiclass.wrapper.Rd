\name{make.multiclass.wrapper}
\alias{make.multiclass.wrapper}
\title{Fuse learner with multiclass method.}
\usage{make.multiclass.wrapper(learner, id=as.character(NA), label=as.character(NA), method,
    codematrix, ...)
}
\description{Fuses a base learner with a multi-class method. Creates a learner object, which can be
used like any other learner object. This way learners which can only handle binary classification 
will be able to handle multi-class problems too.}
\value{\code{\linkS4class{learner}}.}
\arguments{\item{learner}{[\code{\linkS4class{learner}} or string]\cr 
Learning algorithm. See \code{\link{learners}}.}
\item{id}{[string] \cr
Id for resulting learner object. If missing, id of "learner" argument is used.}
\item{label}{[string] \cr
Label for resulting learner object. If missing, label of "learner" argument is used.}
\item{method}{[string] \cr
Currently unsupported.}
\item{codematrix}{[matrix] \cr
ECOC codematrix with entries +1,-1,0. Columns define new binary problems, rows correspond to classes.}
\item{...}{[any] \cr
Optional parameters. Not used currently.}
}
