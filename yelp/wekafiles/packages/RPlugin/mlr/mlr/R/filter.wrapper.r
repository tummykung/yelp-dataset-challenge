
#' Wrapper class for learners to filter variables. Experimental. Can currently 
#' only filter to manually selected variables. 
#' 
#' @exportClass filter.wrapper
#' @title Wrapper class for learners to filter variables.

#' @exportClass filter.wrapper
setClass(
		"filter.wrapper",
		contains = c("base.wrapper"),
		representation = representation(
				vars = "character"
		)
)

#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("filter.wrapper"),
		def = function(.Object, learner, vars) {
			.Object@vars = vars
			callNextMethod(.Object, learner)
		}
)


#' Fuses a base learner with a filter method. Creates a learner object, which can be
#' used like any other learner object. 
#' Currently only filtering to manually selected variables is supported.
#'
#' @param learner [\code{\linkS4class{learner}} or string]\cr 
#'        Learning algorithm. See \code{\link{learners}}.  
#' @param id [string] \cr
#'        Id for resulting learner object. If missing, id of "learner" argument is used.
#' @param label [string] \cr
#'        Label for resulting learner object. If missing, label of "learner" argument is used.
#' @param vars [character]\cr 
#'        Selected variables.  
#' 
#' @return \code{\linkS4class{learner}}.
#' 
#' @title Fuse learner with filter method.
#' @export
make.filter.wrapper = function(learner, id=as.character(NA), label=as.character(NA), vars) {
	new("filter.wrapper", learner=learner, id=id, label=label, vars=vars)
}



#' @rdname train.learner
setMethod(
		f = "train.learner",
		signature = signature(
				.learner="filter.wrapper", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {	
			vars = .learner@vars
			vars = c(vars, .targetvar)
			.data = .data[, vars, drop=FALSE]
			callNextMethod(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...)
		}
)

