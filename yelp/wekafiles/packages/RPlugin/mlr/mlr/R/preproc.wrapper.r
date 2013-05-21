#' @include base.wrapper.r

setClass(
		"preproc.wrapper",
		contains = c("base.wrapper"),
		representation = representation(
				fun = "function",
				defaults = "list"
		)
)

#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("preproc.wrapper"),
		def = function(.Object, learner, id, label, fun, par.descs, par.vals) {
			.Object@fun = fun
			callNextMethod(.Object, learner=learner, id=id, label=label, par.descs=par.descs, par.vals=par.vals)
		}
)


#' Fuses a base learner with a preprocessing method. Creates a learner object, which can be
#' used like any other learner object, but which internally preprocesses the data as requested. 
#' If the train or predict function is called on it, the preprocessing is always invoked before.
#'
#' @param learner [\code{\linkS4class{learner}} or string]\cr 
#'        Learning algorithm. See \code{\link{learners}}.  
#' @param id [string] \cr
#'        Id for resulting learner object. If missing, id of "learner" argument is used.
#' @param label [string] \cr
#'        Label for resulting learner object. If missing, label of "learner" argument is used.
#' @param fun [function] \cr
#'        Function to preprocess a data.frame. First argument must be called 'data', which will be preprocessed and subsequently returned.
#' @param ... [any] \cr
#'        Optional parameters to control the preprocessing. Passed to fun.   
#' 
#' @return \code{\linkS4class{learner}}.
#' 
#' @title Fuse learner with preprocessing.
#' @export

make.preproc.wrapper = function(learner, id=as.character(NA), label=as.character(NA), fun, ...) {
	if (is.character(learner))
		learner = make.learner(learner)
	ns = names(formals(fun))
	args = list(...)
	if (ns[1] != "data")
		stop("First argument in preproc function has to be data without a default value!")		
	ns = ns[-1]
	if (!setequal(names(args), ns))
		stop("All arguments of preproc function except 'data' need default values passed in ... argument.")
	pds = list()
	pvs = list()
	for (i in seq(length=length(ns))) {
		n = ns[i]
		p = args[[n]]
		pds[[i]] = new("par.desc.unknown", par.name=n, when="both", default=p)
		pvs[[n]] = p
	}
	new("preproc.wrapper", learner=learner, id=id, label=label, fun=fun, par.descs=pds, par.vals=pvs)
}


#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="preproc.wrapper", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="ANY" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			fun.args = .learner["par.vals.name", par.top.wrapper.only=TRUE]
			ww = .learner
			fun.args = list(...)[fun.args]		
			fun.args$data = .data
			.data = do.call(.learner@fun, fun.args)
			callNextMethod(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "preproc.wrapper", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			fun.args = .model@learner["par.vals", par.top.wrapper.only=TRUE]
			fun.args$data = .newdata	
			.newdata = do.call(.learner@fun, fun.args)
			callNextMethod(.learner, .model, .newdata, .type, ...)
		}
)	





