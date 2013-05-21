\name{resample.fit}
\alias{resample.fit-methods}
\alias{resample.fit}
\title{Fit models according to a resampling strategy.}
\usage{resample.fit(learner, task, resampling, par.vals, vars, extract)}
\description{Given a resampling strategy, which defines sets of training and test indices, 
\code{resample.fit} fits the selected learner using the training sets and performs predictions for the test sets. 
For construction of the resampling strategies use the factory methods \code{\link{make.res.desc}} and 
\code{\link{make.res.instance}}.}
\details{\code{resample.fit-methods}: Optionally information from the fitted models, e.g. the complete model, can be extracted and returned.

}
\value{\code{resample.fit-methods}: \code{\linkS4class{resample.prediction}}.

}
\arguments{\item{learner}{[\code{\linkS4class{learner}} or \code{\link{character}}]\cr 
Learning algorithm.}
\item{task}{[\code{\linkS4class{learn.task}}] \cr
Learning task.}
\item{resampling}{[\code{\linkS4class{resample.desc}} or \code{\linkS4class{resample.instance}}] \cr
Resampling strategy.}
\item{par.vals}{[list] \cr 
Named list of hyperparameter values. Will overwrite the ones specified in the learner object. Default is empty list.}
\item{vars}{[\code{\link{character}}] \cr 
Vector of variable names to use in training the model. Default is to use all variables.}
\item{extract}{[\code{\link{function}}] \cr 
Function used to extract information from fitted models, e.g. can be used to save the complete list of fitted models. 
Default is to extract nothing.}
}
\alias{resample.fit,learner,learn.task,resample.instance,list,character,function-method}
\alias{resample.fit}
