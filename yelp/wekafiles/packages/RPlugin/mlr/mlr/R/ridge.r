#' @include learnerR.r
roxygen()


setClass(
		"regr.ridge", 
		contains = c("rlearner.regr")
)


setMethod(
		f = "initialize",
		signature = signature("regr.ridge"),
		def = function(.Object) {

			desc = new("learner.desc.regr",
					missings = TRUE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					weights = FALSE
			)
			
			callNextMethod(.Object, label="ridge regression", pack="penalized", desc=desc)
		}
)



#' @rdname train.learner


setMethod(
		f = "train.learner",
		signature = signature(
				.learner="regr.ridge", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="missing" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, ...) {
			f = as.formula(paste(.targetvar, "~."))
			args = list(...)
			i = which(names(args) == "lambda") 
			if (length(i) > 0) {
				names(args)[i] = "lambda2"
			}
			pars <- list(f, data=.data)
			pars <- c(pars, args)
			do.call(penalized, pars)
		}
)


#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "regr.ridge", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "missing" 
		),
		
		def = function(.learner, .model, .newdata, ...) {
			m <- .model["learner.model"]
			.newdata[, .model["target"]] <- 0
			predict(m, data=.newdata,  ...)[,"mu"]
		}
)	

