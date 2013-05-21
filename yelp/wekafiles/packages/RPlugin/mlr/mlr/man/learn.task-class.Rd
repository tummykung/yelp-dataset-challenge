\name{learn.task-class}
\alias{learn.task-class}
\alias{learn.task}
\title{Base class for learning tasks.}
\description{A learning task is a general description object for a machine learning experiment. 
It wraps the data source and specifies - through its subclasses - the type of the task (e.g. classification or regression), 
the target variable and other details of the problem. As this is just an abstract base class, 
you should not instantiate it directly but use the inheriting classes and their factory methods.}
\details{\code{learn.task-class}: Getter.\cr
Note that all getters of \code{\linkS4class{task.desc}} and \code{\linkS4class{data.desc}} can also be used. 

\describe{
\item{data [data.frame]. Optional parameters: row, col}{The data.frame is returned, possibly indexed by row/col. If col is missing, only columns which were not excluded are returned.}
\item{input.names [character]}{The names of the input variables (without excluded variables).}
\item{targets [character]. Optional parameters: row}{If row is missing all target values are returned. Otherwise they are indexed by row.}
\item{weights [numeric]. Optional parameters: row}{If row is missing all case weights are returned. Otherwise they are indexed by row. NULL if no weights were set.}
\item{rows.with.missing [integer]}{Index vector for rows which contain missing values.}
\item{cols.with.missing [integer]}{Index vector for columns which contain missing values.}
\item{rows.with.inf [integer]}{Index vector for rows which contain infinite numerical values.}
\item{cols.with.inf [integer]}{Index vector for columns which contain infinite numerical values.}
}

}
\seealso{\code{\link{make.task}}}
\section{Extends}{\code{\linkS4class{object}}}
\section{Methods}{\describe{\item{\code{\link[=get.learners,learn.task-method]{get.learners}}}{}\item{\code{\link[=resample.update,resample.instance,learn.task,wrapped.model,prediction-method]{resample.update}}}{}\item{\code{\link[=resample.fit,learner,learn.task,resample.instance,list,character,function-method]{resample.fit}}}{}\item{\code{\link[=make.res.instance,character,learn.task,missing,integer-method]{make.res.instance}}}{}\item{\code{\link[=make.res.instance,resample.desc,learn.task,missing,missing-method]{make.res.instance}}}{}\item{\code{\link[=[,learn.task-method]{[}}}{}\item{\code{\link[=train,learner,learn.task,numeric,list,character,character-method]{train}}}{}}}
\alias{[,learn.task-method}

