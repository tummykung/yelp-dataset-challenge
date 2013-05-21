
#' Abstract base class for control objects for optimization. 
#' Cannot be instatiated. 
#' 
#' Getter.\cr
#' 
#' \describe{
#'  \item{opt.type [string]}{'tune' or 'varsel'.}
#' }
#' 
#' @exportClass opt.control
#' @seealso \code{\linkS4class{tune.control}}, \code{\linkS4class{varsel.control}} 
#' @title Base class for control objects for optimization.

setClass(
		"opt.control",
		contains = c("object"),
		representation = representation(
				minimize = "logical",
				tune.threshold = "logical", 
				thresholds = "numeric",
				path = "logical",
				extra.args = "list"
		)
)

#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("opt.control"),
		def = function(.Object, minimize, tune.threshold, thresholds, path, ...) {
			if (missing(minimize))
				return(.Object)
			.Object@minimize = minimize
			.Object@tune.threshold = tune.threshold 			
			.Object@thresholds = thresholds 
			.Object@path = path
			.Object@extra.args = list(...)
			return(.Object)
		}
)


#' @rdname opt.control-class

setMethod(
		f = "[",
		signature = signature("opt.control"),
		def = function(x,i,j,...,drop) {
			if (i == "opt.type"){
				if (is(x, "tune.control"))
					return("tune")
				else
					return("varsel")
			}
			callNextMethod()
		}
)


