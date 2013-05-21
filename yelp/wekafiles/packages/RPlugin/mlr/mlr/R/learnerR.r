#' @include object.r
roxygen()
#' @include learner.r
roxygen()

#' Wraps an already implemented learning method from R to make it accessible to mlr.
#'  
#' Getter.\cr
#' 
#' @exportClass rlearner
#' @title Base class for inducers. 

setClass(
		"rlearner",
		contains = c("learner"),
		representation = representation(
		)
)

#' Getter.
#' @rdname rlearner-class

setMethod(
		f = "[",
		signature = signature("rlearner"),
		def = function(x,i,j,...,drop) {
			if (i == "is.classif") {
				return(is(x, "rlearner.classif"))
			}
			if (i == "is.regr") {
				return(is(x, "rlearner.regr"))
			}
			callNextMethod()
		}
)

#---------------- constructor---- -----------------------------------------------------

#' Constructor.
#' @title rlearner constructor
setMethod(
		f = "initialize",
		signature = signature("rlearner"),
		def = function(.Object, id, label, pack, desc, par.descs=list(), par.vals=list()) {
			if (missing(desc))
				return(.Object)
			if (missing(id))
				id = as.character(class(.Object))
			if (missing(label))
				label = id
			callNextMethod(.Object, id=id, label=label, pack=pack, desc=desc, par.desc=par.descs, par.vals=par.vals)
		}
)

#' Base class for classification learners.
#' @exportClass rlearner.classif
#' @title Base class for classification learners. 
setClass("rlearner.classif", contains = c("rlearner"))


#' Base class for regression learners.
#' @exportClass rlearner.regr
#' @title Base class for regression learners. 
setClass("rlearner.regr", contains = c("rlearner"))

