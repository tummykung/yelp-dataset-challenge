\name{errorhandler.setup}
\alias{errorhandler.setup}
\title{Errorhandler setup.}
\usage{errorhandler.setup(on.learner.error="warn", on.par.without.desc="warn",
    on.convert.var="warn")
}
\description{Sets up the error handling system of mlr.}
\value{NULL.}
\arguments{\item{on.learner.error}{[string] \cr
What should happen if an error in an underlying learning algorithm is caught: 'stop', 'warn' or 'quiet'.\cr 
'stop': R exception is generated.\cr
'warn': Model \code{\linkS4class{learner.failure}} will be created, which predicts only NAs and a warning will be generated.\cr 
'quiet': Same as 'warn' but withou the warning.\cr
Default is 'warn'.}
\item{on.par.without.desc}{[string] \cr
What should happen if a parameter of a learner is set to a value, but no parameter description object exists, indicating a possibly wrong name: 'stop', 'warn' or quiet.\cr
'stop': R exception is generated.\cr
'warn': Warning, but parameter is still passed along to learner.\cr 
'quiet': Same as 'warn' but without the warning.\cr
Default is 'warn'.}
\item{on.convert.var}{[string] \cr
What should happen if an a variable is converted during the creation of a \code{\linkS4class{learn.task}}: 'warn' or 'quiet'.\cr 
Currently, integers are converted to numerics, characters to factors and for classification, target levels without corresponding observation are dropped.\cr 
'warn': Warning on conversion.\cr
'quiet': Conversion done quietly.\cr
Default is 'warn'.}
}
