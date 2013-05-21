\name{make.preproc.wrapper}
\alias{make.preproc.wrapper}
\title{Fuse learner with preprocessing.}
\usage{make.preproc.wrapper(learner, id=as.character(NA), label=as.character(NA), fun, ...)
}
\description{Fuses a base learner with a preprocessing method. Creates a learner object, which can be
used like any other learner object, but which internally preprocesses the data as requested. 
If the train or predict function is called on it, the preprocessing is always invoked before.}
\value{\code{\linkS4class{learner}}.}
\arguments{\item{learner}{[\code{\linkS4class{learner}} or string]\cr 
Learning algorithm. See \code{\link{learners}}.}
\item{id}{[string] \cr
Id for resulting learner object. If missing, id of "learner" argument is used.}
\item{label}{[string] \cr
Label for resulting learner object. If missing, label of "learner" argument is used.}
\item{fun}{[function] \cr
Function to preprocess a data.frame. First argument must be called 'data', which will be preprocessed and subsequently returned.}
\item{...}{[any] \cr
Optional parameters to control the preprocessing. Passed to fun.}
}
