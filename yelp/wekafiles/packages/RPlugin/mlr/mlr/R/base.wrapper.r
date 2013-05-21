#' @include learner.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()
#' @include set.hyper.pars.r
roxygen()
#' @include set.id.r
roxygen()
#' @include set.label.r
roxygen()

#' Abstract base class to wrap something around a learner.
#' @exportClass base.wrapper

setClass(
		"base.wrapper",
		contains = c("learner"),
		representation = representation(
			learner = "learner"
		)
)


#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("base.wrapper"),
		def = function(.Object, learner, id, label, desc, par.descs, par.vals, pack=as.character(c())) {
			if (missing(learner))
				return(.Object)
			.Object@learner = learner
			if (is.na(id))
				id = learner["id"]
			if (is.na(label))
				label = learner["label"]
			if (missing(desc))
				desc = learner@desc
			callNextMethod(.Object, id=id, label=label, desc=desc, par.descs=par.descs, par.vals=par.vals, pack=pack)
		}
)



#' @rdname base.wrapper-class

setMethod(
		f = "[",
		signature = signature("base.wrapper"),
		def = function(x,i,j,...,drop) {
			check.getter.args(x, c("par.top.wrapper.only", "par.when"), j, ...)
			if(i == "id") {
				return(x@id)
			}			
			if(i == "label") {
				return(x@label)
			}			
			if (i == "learner")
				return(x@learner)
			if(i == "pack") {
				return(c(x@learner["pack"], x@pack))
			}			
			
			args = list(...)
			par.top.wrapper.only = args$par.top.wrapper.only
			if (is.null(par.top.wrapper.only)) par.top.wrapper.only=FALSE
			if(i == "par.descs") {
				if(par.top.wrapper.only) 
					return(callNextMethod())
				else {
					return(c(x@learner["par.descs", ...], x["par.descs", par.top.wrapper.only=TRUE, ...]))
				}					
			}
			if(i == "par.descs.name") {
				if(par.top.wrapper.only) 
					return(callNextMethod())
				else {
					return(c(x@learner["par.descs.name", ...], x["par.descs.name", par.top.wrapper.only=TRUE, ...]))
				}					
			}
			if(i == "par.descs.when") {
				if(par.top.wrapper.only) 
					return(callNextMethod())
				else {
					return(c(x@learner["par.descs.when", ...], x["par.descs.when", par.top.wrapper.only=TRUE, ...]))
				}					
			}
			if(i == "par.vals") {
				if(par.top.wrapper.only) 
					return(callNextMethod())
				else {
					return(c(x@learner["par.vals", ...], x["par.vals", par.top.wrapper.only=TRUE, ...]))
				}					
			}
			if(i == "par.vals.name") {
				if(par.top.wrapper.only) 
					return(callNextMethod())
				else {
					return(c(x@learner["par.vals.name", ...], x["par.vals.name", par.top.wrapper.only=TRUE, ...]))
				}					
			}
			return(x@learner[i])
		}
)


#' @rdname train.learner
setMethod(
		f = "train.learner",
		signature = signature(
				.learner="base.wrapper", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="ANY" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			args = list(...)
			args = args[!(names(args) %in% .learner["par.vals.name", par.top.wrapper.only=TRUE])]
			f.args = list(.learner@learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs)
			f.args = c(f.args, args)
			do.call(train.learner, f.args)
		}
)

#' @rdname pred.learner
setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "base.wrapper", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "ANY" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			args = list(...)
			args = args[!(names(args) %in% .learner["par.vals.name", par.top.wrapper.only=TRUE])]
			f.args = list(.learner@learner, .model, .newdata, .type)
			f.args = c(f.args, args)
			do.call(pred.learner, f.args)
		}
)	

#' @rdname set.hyper.pars 
setMethod(
	f = "set.hyper.pars",
	
	signature = signature(
			learner="base.wrapper", 
			par.vals="list" 
	),
	
	def = function(learner, ..., par.vals=list()) {
		ns = names(par.vals)
		pds.n = learner["par.descs.name", par.top.wrapper.only=TRUE]
		for (i in seq(length=length(par.vals))) {
			if (ns[i] %in% pds.n) {
				learner = callNextMethod(learner, par.vals=par.vals[i])
			} else {	
				learner@learner = set.hyper.pars(learner@learner, par.vals=par.vals[i])
			}
		}
		return(learner)
	} 
)

