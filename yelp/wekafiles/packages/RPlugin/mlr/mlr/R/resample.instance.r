#' @include resample.desc.r
roxygen()

#' Base class for specific resampling draws like cross-validation or bootstrapping.
#' This class encapsulates training and test sets generated from the data set for a number of iterations. 
#' It mainly stores a set of integer vectors indicating the training examples for each iteration.
#' Don't create objects from this class directly but use the corresponding subclasses.
#' For construction simply use the factory method \code{\link{make.res.instance}}. 
#' 
#' Getter.
#' 
#' \describe{
#' 	\item{size [integer]}{Number of observations.}
#' 	\item{name [character]}{The name of the resample description object, i.e. the type of resampling.}
#' 	\item{iters [integer]}{The number of resampling iterations.}
#'  \item{train.inds [list | integer] Optional parameter: j}{If j is a single integer, the vector of training indices for the jth iteration. Otherwise, the list of indices for iterations. Missing j means list of all indices.}
#'  \item{test.inds [list | integer] Optional parameter: j}{If j is a single integer, the vector of test indices for the jth iteration. Otherwise, the list of indices for iterations j. Missing j means list of all indices.}
#' }
#' 
#' @rdname resample.instance-class
#' 
#' @note If you want to add another resampling strategy, have a look at the web documentation. 
#' @exportClass resample.instance
#' @seealso \code{\linkS4class{resample.desc}}, \code{\link{make.res.instance}}, \code{\link{resample.fit}} 
#' @title Resampling instance.


# todo validation for size
setClass(
		"resample.instance",   
		contains = c("object"), 
		# we always have to store training inds because the order might matter
		representation = representation(
				desc = "resample.desc", 
				size = "integer",
				inds = "list"
		)
)


#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("resample.instance"),
		def = function(.Object, desc, size, inds) {
			if (missing(desc))
				return(.Object)
			.Object@desc = desc
			if (round(size) != size)
				error("You passed a non-integer to arg 'size' of resample.instance!")
			.Object@size = as.integer(size)
			.Object@inds = inds
			return(.Object)
		}
)

#' @rdname resample.instance-class

setMethod(
		f = "[",
		signature = signature("resample.instance"),
		def = function(x,i,j,...,drop) {
			if (i == "size")
				return(x@size)
			
			if (i == "iters")
				return(length(x@inds))
			
			return(x@desc[i,...,drop=drop])
		}
)



#' @rdname to.string

setMethod(
		f = "to.string",
		signature = signature("resample.instance"),
		def = function(x) {
			return(
					paste(
							"Instance for ", x@desc@name,  " with ", x["iters"], " iterations and ", x@size, " cases\n",
							paste(capture.output(str(x@inds)), collapse="\n"), 
							"\n", sep=""
					)
			)
		}
)


setClass(
		"resample.instance.seq", 
		contains = c("resample.instance")
)


setClass(
		"resample.instance.nonseq", 
		contains = c("resample.instance")
)


