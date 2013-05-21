\name{make.learner}
\alias{make.learner}
\title{Create learner object.}
\usage{make.learner(class, id, label, predict.type="response",
    predict.threshold=numeric(0), ..., par.vals=list())
}
\description{Create learner object.}
\value{\code{\linkS4class{learner}}.}
\arguments{\item{class}{[string] \cr
Class of learner to create.}
\item{id}{[string]\cr 
Id string for object. Used to select the object from a named list, etc.}
\item{label}{[string]\cr 
Label string for object. Used in plots, etc.}
\item{predict.type}{[string] \cr
Classification: "response" | "prob" | "decision", specifying the type to
predict. Default is "response". "decision" is experimental. Ignored for
regression.}
\item{predict.threshold}{[numeric] \cr
Threshold to produce class labels if type is not "response". 
Currently only supported for binary classification and type="prob", where it
represents the required predicted probability for the positive class, so that
a positive class is predicted as "response". Default is 0.5 for type="prob".
Ignored for regression.}
\item{...}{[any] \cr
Optional named (hyper)parameters. Alternatively, you can pass via the "par.vals" argument.}
\item{par.vals}{[list] \cr
Optional list of named (hyper)parameters. Alternatively, you can pass via the ... argument.}
}
