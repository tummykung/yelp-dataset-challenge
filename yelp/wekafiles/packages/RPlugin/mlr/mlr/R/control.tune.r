#' @include opt.control.r
roxygen()

#' Abstract base class for control objects for tuning. 
#' Cannot be instatiated. 
#' 
#' @exportClass tune.control
#' @seealso \code{\linkS4class{grid.control}}, \code{\linkS4class{optim.control}}, \code{\linkS4class{cmaes.control}} 
#' @title Base class for control objects for tuning.

setClass(
		"tune.control",
		contains = c("opt.control"),
		representation = representation(
				start = "list",
				par.descs = "list",
				scale = "function"
		)
)

#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("tune.control"),
		def = function(.Object, minimize, tune.threshold, thresholds, path, start, par.descs, scale, ...) {
			if (missing(minimize))
				return(.Object)
			.Object@start = start 			
			.Object@par.descs = par.descs 			
			.Object@scale = scale 		
			.Object = callNextMethod(.Object=.Object, minimize=minimize, 
					tune.threshold=tune.threshold, thresholds=thresholds, path=path, ...)
			return(.Object)
		}
)


#' @rdname tune.control-class

setMethod(
		f = "[",
		signature = signature("tune.control"),
		def = function(x,i,j,...,drop) {
			pds = x@par.descs
			if (i == "par.names") {
				return(sapply(pds, function(y) y@par.name))
			}
			if (i == "lower") {
				y = sapply(pds, function(y) ifelse(is(y, "par.desc.num"), y@lower, NA))
				names(y) = x["par.names"] 
				return(y)
			}
			if (i == "upper") {
				y = sapply(pds, function(y) ifelse(is(y, "par.desc.num"), y@upper, NA))
				names(y) = x["par.names"] 
				return(y)
			}		
			callNextMethod(x,i,j,...,drop=drop)
		}
)
