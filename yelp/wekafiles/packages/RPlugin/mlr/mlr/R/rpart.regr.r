#' @include learnerR.r
roxygen()

setClass(
		"regr.rpart", 
		contains = c("rlearner.regr")
)


setMethod(
		f = "initialize",
		signature = signature("regr.rpart"),
		def = function(.Object) {
			
			desc = new("learner.desc.regr",
					missings = TRUE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					weights = TRUE
			)
			callNextMethod(.Object, label="RPART", pack="rpart",	desc=desc, )
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="regr.rpart", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="missing"
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			f = as.formula(paste(.targetvar, "~."))
			rpart(f, data=.data, weights=.weights, ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "regr.rpart", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "missing" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			predict(.model["learner.model"], newdata=.newdata, ...)
		}
)	


