\name{learner-class}
\alias{learner-class}
\alias{learner}
\title{Base class for inducers.}
\description{Abstract base class for learning algorithms.}
\details{\code{learner-class}: Getter.\cr
Note that all getters of \code{\linkS4class{learner.desc}} can also be used. 

\describe{
\item{is.classif [boolean]}{Is this learner for classification tasks?}
\item{is.regr [boolean]}{Is this learner for regression tasks?}
\item{id [string]}{Id string of learner.}
\item{label [string]}{Label string of learner.}
\item{pack [string]}{Package were underlying learner is implemented.}
\item{desc [\code{\linkS4class{learner.desc}}]}{Properties object to describe functionality of the learner.}
\item{par.vals [list]}{List of fixed hyperparameters and respective values for this learner.}
\item{par.vals.name [character]}{Names of currently fixed hyperparameters.}
\item{par.descs [list]}{Named list of \code{\linkS4class{par.desc}} description objects for all possible hyperparameters for this learner.}
\item{par.descs.name [character]}{Names of all hyperparameters for which description objects exist.}
\item{par.descs.when [character]}{Named character vector. Specifies when a cetrain hyperparameter is used. Possible entries are 'train', 'predict' or 'both'.}
\item{predict.type [character]}{What should be predicted: 'response', 'prob' or 'decision'.}
\item{predict.threshold [character]}{Threshold to produce class labels if type is not "response".} 
}

}
\section{Extends}{\code{\linkS4class{object}}}
\section{Methods}{\describe{\item{\code{\link[=[,learner-method]{[}}}{\code{signature( = "learner")}: Getter.}\item{\code{\link[=to.string,learner-method]{to.string}}}{}\item{\code{\link[=resample.fit,learner,learn.task,resample.instance,list,character,function-method]{resample.fit}}}{}\item{\code{\link[=set.hyper.pars,learner,list-method]{set.hyper.pars}}}{}\item{\code{\link[=set.id,learner,character-method]{set.id}}}{}\item{\code{\link[=set.label,learner,character-method]{set.label}}}{}\item{\code{\link[=train,learner,learn.task,numeric,list,character,character-method]{train}}}{}}}
\alias{[,learner-method}

