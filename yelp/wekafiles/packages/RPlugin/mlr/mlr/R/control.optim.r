#' @include control.tune.r
roxygen()

#' @exportClass optim.control
#' @rdname optim.control 

setClass(
		"optim.control",
		contains = c("tune.control")
)


#' Control structure for tuning with optim (Nelder-Mead, SANN, etc). 
#' 
#' @param minimize [logical] \cr 
#'       Minimize performance measure? Default is TRUE. 
#' @param tune.threshold [logical] \cr 
#'		Perform empirical thresholding? Default is FALSE. Only supported for binary classification and you have to set predict.type to "prob" for this in make.learner. 
#' @param thresholds [numeric] \cr 
#'		Number of thresholds to try in tuning. Predicted probabilities are sorted and divided into groups of equal size. Default is 10. 		        
#' @param path [boolean]\cr
#'        Should optimization path be saved?
#' @param start [numeric] \cr
#'		Named vector of initial values.
#' @param lower [numeric] \cr
#'		Named vector of lower boundary constraints. Default is -Inf. Only used in "L-BFGS-B".
#' @param upper [numeric] \cr
#'		Named vector of upper boundary constraints. Default is Inf. Only used in "L-BFGS-B".
#' @param scale [\code{\link{function}}] \cr 
#'		A function to scale the hyperparameters. E.g. maybe you want to optimize in some log-space.
#'		Has to take a vector and return a scaled one. Default is identity function.
#' @param ... Further control parameters passed to the \code{control} argument of \code{\link[stats]{optim}}.
#' 		    
#' @return Control structure for tuning.
#' @exportMethod optim.control
#' @rdname optim.control 
#' @title Control for tuning with optim. 


setGeneric(
		name = "optim.control",
		def = function(minimize, tune.threshold, thresholds, path, start, lower, upper, scale, ...) {
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
			if (missing(start))
				stop("You have to provide a start value!")
			if (missing(lower))
				{lower=start;lower[]=-Inf}	
			if (length(lower)==1)
				lower = rep(lower, length(start))
			if (is.null(names(lower)))
				names(lower) = names(start)
			if (missing(upper))
				{upper=start;upper[]=Inf}				
			if (length(upper)==1)
				upper = rep(upper, length(start))
			if (is.null(names(upper)))
				names(upper) = names(start)
			if (missing(scale))
				scale=identity
			standardGeneric("optim.control")
		}
)


#' @rdname optim.control 

setMethod(
		f = "optim.control",
		signature = signature(minimize="logical", tune.threshold="logical", thresholds="integer", path="logical", start="numeric", lower="numeric", upper="numeric", scale="function"),
		def = function(minimize, tune.threshold, thresholds, path, start, lower, upper, scale, ...) {
			pds = list()
			for (i in 1:length(start)) {
				pd = new("par.desc.num", par.name=names(start)[i], lower=lower[i], upper=upper[i])
				pds[[i]] = pd 
			}
			new("optim.control", minimize=minimize, tune.threshold=tune.threshold, thresholds=thresholds, path=path,
					start=as.list(start), par.descs=pds, scale=scale, ...)
		}
)

