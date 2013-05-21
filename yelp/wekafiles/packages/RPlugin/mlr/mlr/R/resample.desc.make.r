



#' Generates a description object for a resampling strategy. 
#' 
#' @param method [string] \cr
#' 	  "cv" for cross-validation, "stratcv" for stratified cross-validation,  "repcv" for repeated cross-validation,\cr
#'		"bs" for out-of-bag bootstrap, "bs632" for B632 bootstrap, "bs632plus" for B632+ bootstrap,\cr
#'    "subsample" for subsampling, "holdout" for holdout.	
#' @param iters [integer] \cr
#'		Number of resampling iterations. Not needed for "holdout". 	 			
#' @param ... [any] \cr
#'		Further parameters for strategies.\cr 
#'			split: Percentage of training cases for "holdout", "subsample".\cr
#'			reps: Repeats for "repcv"
#' 
#' @return \code{\linkS4class{resample.desc}}.
#' @export 
#' @title Construct resampling description.



make.res.desc = function(method, iters, ...) {
	cc = paste(method, "desc", sep=".")
	if (!missing(iters)) {
		iters = as.integer(iters)
		return(new(cc, iters=iters, ...))
	} else {
		return(new(cc, ...))
	}
	
		
}
