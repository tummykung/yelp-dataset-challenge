#' @include object.r
roxygen()
#' @include data.desc.r
roxygen()
#' @include task.desc.r
roxygen()

#' A learning task is a general description object for a machine learning experiment. 
#' It wraps the data source and specifies - through its subclasses - the type of the task (e.g. classification or regression), 
#' the target variable and other details of the problem. As this is just an abstract base class, 
#' you should not instantiate it directly but use the inheriting classes and their factory methods.
#'  
#' Getter.\cr
#' Note that all getters of \code{\linkS4class{task.desc}} and \code{\linkS4class{data.desc}} can also be used. 
#' 
#' \describe{
#' 	\item{data [data.frame]. Optional parameters: row, col}{The data.frame is returned, possibly indexed by row/col. If col is missing, only columns which were not excluded are returned.}
#'  \item{input.names [character]}{The names of the input variables (without excluded variables).}
#'  \item{targets [character]. Optional parameters: row}{If row is missing all target values are returned. Otherwise they are indexed by row.}
#'  \item{weights [numeric]. Optional parameters: row}{If row is missing all case weights are returned. Otherwise they are indexed by row. NULL if no weights were set.}
#'  \item{rows.with.missing [integer]}{Index vector for rows which contain missing values.}
#'  \item{cols.with.missing [integer]}{Index vector for columns which contain missing values.}
#'  \item{rows.with.inf [integer]}{Index vector for rows which contain infinite numerical values.}
#'  \item{cols.with.inf [integer]}{Index vector for columns which contain infinite numerical values.}
#' }
#' 
#' @exportClass learn.task
#' @seealso \code{\link{make.task}}
#' @title Base class for learning tasks.


setClass(
		"learn.task",
		contains = c("object"),
		representation = representation(
				data = "data.frame",
				weights = "numeric",
				blocking = "factor",
				data.desc = "data.desc",
				task.desc = "task.desc"
		)
)


#---------------- constructor---- -----------------------------------------------------

#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("learn.task"),
		def = function(.Object, data, weights, blocking, data.desc, task.desc) {
			
			# constructor is called in setClass of inheriting classes 
			# wtf chambers, wtf!
			if(missing(data))
				return(.Object)					
			
			.Object@data = data
			.Object@weights = weights
			.Object@blocking = blocking
			.Object@data.desc = data.desc
			.Object@task.desc = task.desc
			
#			.Object@data.desc <- make.data.desc(.Object["data"], target)
			
			return(.Object)
		}
)

#' @rdname learn.task-class

setMethod(
		f = "[",
		signature = signature("learn.task"),
		def = function(x,i,j,...,drop) {
			check.getter(x,i,j,...,drop)
			args = list(...)
			argnames = names(args)
			
			dd = x@data.desc
			td = x@task.desc
			row = args$row
			col = args$col
			
			if (i == "target.name") {
				return(dd["target"])
			}
			if (i == "input.names"){
				return(setdiff(colnames(x@data), c(x["excluded"], x["target.name"])))
			}
			
			if (is.null(row))
				row = 1:nrow(x@data)
			
			if (i == "targets") {
				return(x@data[row, x["target.name"]])
			}
			if (i == "weights") {
				if (!td["has.weights"])
					return(NULL)
				return(x@weights[row])
			}
			if (i == "blocking") {
				if (!td["has.blocking"])
					return(NULL)
				return(x@blocking[row])
			}
			if (i == "data"){
				if (is.null(col))
					col = setdiff(colnames(x@data), x["excluded"])
				if (missing(drop))
					drop = (length(col) == 1)
				return(x@data[row, col, drop=drop])				
			}
			if (i == "rows.with.missing"){
				return(sum(apply(x["data"], 1, function(x) any(is.na(x)))))
			}
			if (i == "cols.with.missing"){
				return(sum(apply(x["data"], 2, function(x) any(is.na(x)))))
			}
			if (i == "rows.with.inf"){
				return(sum(apply(x["data"], 1, function(x) any(is.infinite(x)))))
			}
			if (i == "cols.with.inf"){
				return(sum(apply(x["data"], 2, function(x) any(is.infinite(x)))))
			}
			y = td[i]
			if (!is.null(y))
				return(y)
			y = dd[i]
			if (!is.null(y))
				return(y)
			
			callNextMethod()
		}
)
