#' Base class for description of resampling algorithms.
#' A description of a resampling algorithm contains all necessary information to provide a resampling.instance, 
#' when given the size of the data set.
#' For construction simply use the factory method \code{\link{make.res.desc}}. 
#' 
#' Getter.
#' 
#' \describe{
#' 	\item{instance.class [character]}{S4 class name of the corresponding resample.instance}
#' 	\item{name [character]}{Name of this resampling algorithm}
#' 	\item{iters [numeric]}{Number of iterations. Note that this the complete number of generated train/test sets, so for a 10 times repeated 5fold cross-validation it would be 50.}
#' 	\item{has.groups [boolean]}{Is special grouping used for predictions of a iteration in order to aggregate them differently?}
#' } 
#' @exportClass resample.desc 
#' @title resample.desc

# todo validation for size
setClass(
		"resample.desc", 
		contains = c("object"),
		representation = representation(
				instance.class = "character", 
				name = "character", 
				iters = "integer",
				aggr.iter = "list",
				aggr.group = "function",
				props = "list"
		)
)


#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("resample.desc"),
		def = function(.Object, instance.class, name, iters, aggr.iter, has.groups, aggr.group, ...) {
			if (missing(name))
				return(.Object)					
			.Object@instance.class = instance.class
			.Object@name = name
			.Object@iters = iters
			if (missing(aggr.iter))
				aggr.iter = list("mean", "sd")				
			.Object@aggr.iter = aggr.iter
			.Object@props = list(...)
			if (has.groups) {
				if (missing(aggr.group))
					aggr.group = function(x, g, rin) colMeans(x)
				.Object@aggr.group = aggr.group
			}				
			.Object@props$has.groups = has.groups
			return(.Object)
		}
)


#' @rdname resample.desc-class

setMethod(
		f = "[",
		signature = signature("resample.desc"),
		def = function(x,i,j,...,drop) {
			if (i %in% c("has.groups", "reps")) {
				return(x@props[[i]])
			}
			callNextMethod(x,i,j,...,drop=drop)
		}
)



#' @rdname to.string

setMethod(
		f = "to.string",
		signature = signature("resample.desc"),
		def = function(x) {
			return(
					paste(
							x["name"], " with ", x@iters, " iterations.\n",	sep=""
					)
			)
		}
)


setClass(
		"resample.desc.seq", 
		contains = c("resample.desc")
)


setClass(
		"resample.desc.nonseq", 
		contains = c("resample.desc")
)



