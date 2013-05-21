# A loss transforms a vector of predictions (compared to the true responses) into a  vector of numerical values, 
# measuring the quality of each prediction individually. 
#
#' Losses can always be passed as a single string (name of a single loss), a character vector of multiple names of losses or 
#' a list containing string names of losses and your own loss functions as function objects. The latter ones should 
#' be named list elements.  
#' 
#' Classification: 
#' \itemize{ 
#' 		\item{\bold{zero-one}}{\cr Zero-one loss}
#' 		\item{\bold{costs}}{\cr Misclassification costs according to cost matrix}
#' }
#' 
#' Regression: 
#' \itemize{ 
#' 		\item{\bold{squared}}{\cr Squared error}
#' 		\item{\bold{abs}}{\cr Absolute error}
#' 		\item{\bold{residual}}{\cr Signed residual error}
#' }
#'  
#' @title Loss functions.
losses = function() {}

make.losses = function(xs) {
	if (length(xs)==0)
		return(list())
	ys = list()
	for (i in 1:length(xs)) {
		x = xs[[i]] 
		if (is.function(x))
			y = x
		else if (is.character(x))
			y =make.loss(x)
		ys[[i]] = y
		nn = names(xs)[i]
		if (is.null(nn))
			nn = attr(y, "id")
		if (is.null(nn))
			stop("No name for loss!")
		names(ys)[i] = nn
	}
	return(ys)	
}


make.loss <- function(name) {
	if (name=="squared") 
		fun=function(x, task) (x["truth"] - x["response"])^2 
	else if (name=="abs") 
		fun=function(x, task) abs(x["truth"] - x["response"]) 
	else if (name=="residual") 
		fun=function(x, task) x["truth"] - x["response"] 
	else if (name=="zero-one") 
		fun=function(x, task) as.numeric(x["truth"] != x["response"]) 
	else if (name=="costs") 
		fun=function(x, task) { 
			cm = x@task.desc["costs"]
			if (all(dim(cm) == 0))
				stop("No costs were defined in task!")
			cc = function(truth, pred) {
				cm[truth, pred]
			}
			unlist(Map(cc, as.character(x["truth"]), as.character(x["response"])))			
		}
	else 	
		stop(paste("Loss", name, "does not exist!"))
	
	attr(fun, "id") = name
	return(fun)
}


default.loss = function() {
	return(list())
}





