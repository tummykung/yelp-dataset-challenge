#' @include control.tune.r
roxygen()

#' @exportClass grid.control
#' @rdname grid.control 

setClass(
		"grid.control",
		contains = c("tune.control")
)

#' Control structure for grid search tuning. 
#' 
#' @param minimize [logical] \cr 
#'       Minimize performance measure? Default is TRUE. 
#' @param tune.threshold [logical] \cr 
#'       Perform empirical thresholding? Default is FALSE. Only supported for binary classification and you have to set predict.type to "prob" for this in make.learner. 
#' @param thresholds [numeric] \cr 
#'		Number of thresholds to try in tuning. Predicted probabilities are sorted and divided into groups of equal size. Default is 10. 		        
#' @param path [boolean]\cr
#'        Should optimization path be saved?
#' @param ranges [\code{\link{list}}] \cr 
#' 		A list of named vectors/lists of possible values for each hyperparameter. 
#'      You can also pass a list of such ranges by using [\code{\link{combine.ranges}}] 
#'      in the rare case when it does not make sense to search a complete cross-product of range values.     
#' @param scale [\code{\link{function}}] \cr 
#'        A function to scale the hyperparameters. E.g. maybe you want to optimize in some log-space.
#'        Has to take a vector and return a scaled one. Default is identity function.
#' 		    
#' @return Control structure for tuning.
#' @exportMethod grid.control
#' @rdname grid.control 
#' @title Control for grid search tuning. 


setGeneric(
		name = "grid.control",
		def = function(minimize, tune.threshold, thresholds, path, ranges, scale) {
			if (missing(minimize))
				minimize=TRUE
			if (missing(tune.threshold))
				tune.threshold=FALSE
			if (missing(thresholds))
				thresholds=10
			if (is.numeric(thresholds))
				thresholds = as.integer(thresholds)
			if (missing(path))
				path=FALSE
			if (missing(ranges))
				ranges=list()
			if (missing(scale))
				scale=identity
			standardGeneric("grid.control")
		}
)


#' @rdname grid.control 

setMethod(
		f = "grid.control",
		signature = signature(minimize="logical", tune.threshold="logical", thresholds="integer", path="logical", ranges="list", scale="function"),
		def = function(minimize, tune.threshold, thresholds, path, ranges, scale) {
			pds = list()
			for (i in 1:length(ranges)) {
				pd = new("par.desc.disc", par.name=names(ranges)[i], vals=as.list(ranges[[i]]))
				pds[[i]] = pd 
			}
			new("grid.control", minimize=minimize, tune.threshold=tune.threshold, thresholds=thresholds, path=path,
					start=list(), par.descs=pds, scale=scale)
		}
)
