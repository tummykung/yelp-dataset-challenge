#' @include learnerR.r
roxygen()

  
setClass(
		"regr.ksvm", 
		contains = c("rlearner.regr")
)



setMethod(
		f = "initialize",
		signature = signature("regr.ksvm"),
		def = function(.Object) {
			
			desc = new("learner.desc.regr",
					missings = FALSE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					weights = FALSE
			)
			
			callNextMethod(.Object, label="SVM", pack="kernlab", desc=desc)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="regr.ksvm", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="missing" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, ...) {
			xs = args.to.control(list, c("degree", "offset", "scale", "sigma", "order", "length", "lambda"), list(...))
			f = as.formula(paste(.targetvar, "~."))
			# difference in missing(kpar) and kpar=list()!
			if (length(xs$control) > 0)
				args = c(list(f, data=.data, fit=FALSE, kpar=xs$control), xs$args)
			else
				args = c(list(f, data=.data, fit=FALSE), xs$args)
			do.call(ksvm, args)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "regr.ksvm", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "missing" 
		),
		
		def = function(.learner, .model, .newdata, ...) {
			predict(.model["learner.model"], newdata=.newdata, ...)[,1]
		}
)	



