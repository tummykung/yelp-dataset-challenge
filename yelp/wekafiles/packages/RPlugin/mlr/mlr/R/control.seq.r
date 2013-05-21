#' @include control.varsel.r
roxygen()

#' @exportClass sequential.control
#' @rdname sequential.control 

setClass(
		"sequential.control",
		contains = c("varsel.control"),
		representation = representation(
				method = "character", 
				alpha = "numeric", 
				beta = "numeric" 
		)
)

#' Constructor.
setMethod(
		f = "initialize",
		signature = signature("sequential.control"),
		def = function(.Object, minimize, tune.threshold, thresholds, path, max.vars, method, alpha, beta) {
			.Object = callNextMethod(.Object, minimize, tune.threshold=tune.threshold, thresholds, path=path, 
					maxit=.Machine$integer.max, max.vars=max.vars)
			.Object@alpha = alpha 			
			.Object@beta = beta 	
			.Object@method = method 			
			return(.Object)
		}
)


#' Control structure for sequential variable selection. 
#' 
#' @param minimize [logical] \cr 
#'       Minimize performance measure? Default is TRUE.
#' @param tune.threshold [logical] \cr 
#'		Perform empirical thresholding? Default is FALSE. Only supported for binary classification and you have to set predict.type to "prob" for this in make.learner. 
#' @param thresholds [numeric] \cr 
#'		Number of thresholds to try in tuning. Predicted probabilities are sorted and divided into groups of equal size. Default is 10. 		        
#' @param path [boolean]\cr
#'        Should optimization path be saved?
#' @param max.vars [integer] \cr 
#'        Maximal number of allowed variables in the final set. Default is max. integer.
#' @param method [\code{\link{character}}] \cr
#'        Search method. Currently supported are sequential forward search "sfs", sequential backward search "sbs", 
#'        sequential floating forward search "sffs", sequential floating backward search "sfbs". Default is "sfs".    
#' @param alpha [numeric] \cr 
#'  	 sfs, sffs: In a forward step, minimal improvement of performance measure. Can be negative.        
#' @param beta [numeric] \cr 
#'  	 sbs, sfbs: In a backward step, minimal improvement of performance measure. Can be negative.        
#' 		    
#' @return Control structure.
#' @exportMethod sequential.control
#' @rdname sequential.control 
#' @title Control structure for sequential variable selection. 


setGeneric(
		name = "sequential.control",
		def = function(minimize, tune.threshold, thresholds, path, max.vars, method, alpha, beta) {
			if (missing(minimize))
				minimize=TRUE
			if (missing(tune.threshold))
				tune.threshold=FALSE
			if (missing(thresholds))
				thresholds=10
			if (is.numeric(thresholds))
				thresholds = as.integer(thresholds)
			if (missing(path))
				path = FALSE
			if (missing(max.vars))
				max.vars = .Machine$integer.max
			if (is.numeric(max.vars))
				max.vars = as.integer(max.vars)
			if (missing(method))
				method="sfs"			
			if (missing(alpha))
				alpha=0.01
			if (missing(beta))
				beta=0.01
			standardGeneric("sequential.control")
		}
)


#' @rdname sequential.control 

setMethod(
		f = "sequential.control",
		signature = signature(minimize="logical", tune.threshold="logical", thresholds="integer", path="logical",
				max.vars="integer", method="character", alpha="numeric", beta="numeric"),
		def = function(minimize, tune.threshold, thresholds, path, max.vars, method, alpha, beta) {
			new("sequential.control", minimize=minimize, tune.threshold=tune.threshold, thresholds=thresholds, path=path, 
					max.vars=max.vars, method=method, alpha=alpha, beta=beta)
		}
)



