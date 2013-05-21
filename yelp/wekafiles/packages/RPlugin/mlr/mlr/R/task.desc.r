#' @include object.r
roxygen()

#' Description object for task.
#' 
#' Getter.\cr
#' 
#' \describe{
#'  \item{id [string]}{Id string of task.}
#'	\item{label [string]}{Label string of task.}
#'  \item{is.classif [boolean]}{Classification task?}
#' 	\item{is.regr [boolean]}{Regression task?}
#'  \item{has.weights [boolean]}{Are weights available in task for covariates?}
#'  \item{has.blocking [boolean]}{Is blocking available in task for observations?}
#'  \item{costs [matrix]}{Cost matrix, of dimension (0,0) if not available.}
#'  \item{positive [string]}{Positive class label for binary classification, NA else.}
#'  \item{negative [string]}{Negative class label for binary classification,, NA else.}
#' }
#' @exportClass task.desc
#' @title Description object for task. 

setClass(
		"task.desc",
		contains = c("object"),
		representation = representation(
				task.class = "character",
				props = "list"
		)
)

#' @rdname task.desc-class
setMethod(
		f = "[",
		signature = signature("task.desc"),
		def = function(x,i,j,...,drop) {
			if (i == "is.classif")
				return(x@task.class == "classif.task")
			if (i == "is.regr")
				return(x@task.class == "regr.task")
			if (i == "id") 
				return(x@props$id)
			if (i == "label") 
				return(x@props$label)
			if (i == "has.weights") 
				return(x@props$has.weights)
			if (i == "has.blocking") 
				return(x@props$has.blocking)
			if (i == "costs") 
				return(x@props$costs)
			if (i == "positive") 
				return(x@props$positive)
			if (i == "negative") 
				return(x@props$negative)
			callNextMethod()
		}
)

#' Constructor.
setMethod(
		f = "initialize",
		signature = signature("task.desc"),
		def = function(.Object, task.class, id, label, has.weights, has.blocking, costs, positive, negative) {
			.Object@task.class = task.class
			.Object@props$id = id
			.Object@props$label = label
			.Object@props$has.weights = has.weights
			.Object@props$has.blocking = has.blocking
			.Object@props$costs = costs
			.Object@props$positive = positive
			.Object@props$negative = negative
			return(.Object)
		}
)




