\name{train}
\alias{train-methods}
\alias{train}
\title{Train a learning algorithm.}
\description{Given a \code{\linkS4class{learn.task}}, creates a model for the learning machine 
which can be used for predictions on new data.}
\value{\code{train-methods}: \code{\linkS4class{wrapped.model}}.

}
\seealso{\code{\link{predict}}}
\arguments{\item{learner}{[\code{\linkS4class{learner}} or string]\cr 
Learning algorithm. See \code{\link{learners}}.}
\item{task}{[\code{\linkS4class{learn.task}}]\cr 
Specifies learning task.}
\item{subset}{[\code{\link{integer}}] \cr 
An index vector specifying the training cases to be used for fitting. By default the complete data set is used.}
\item{par.vals}{[list] \cr 
Named list of hyperparameter values. Will overwrite the ones specified in the learner object. Default is empty list.}
\item{vars}{[\code{\link{character}}] \cr
Vector of variable names to use in training the model. Default is to use all variables, except the excluded in the task.}
\item{type}{[string] \cr
Classification: "response" | "prob" | "decision", specifying the type to predict later.
Default is "response". Very rarely you have to set this during training as well, as the fitted models differ.}
}
\alias{train,learner,learn.task,numeric,list,character,character-method}
\alias{train}
