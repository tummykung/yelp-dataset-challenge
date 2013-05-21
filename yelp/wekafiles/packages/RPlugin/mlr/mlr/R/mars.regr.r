#' @include learnerR.r
roxygen()

setClass(
		"regr.mars", 
		contains = c("rlearner.regr")
)


setMethod(
		f = "initialize",
		signature = signature("regr.mars"),
		def = function(.Object) {
			
			desc = new("learner.desc.regr",
					missings = FALSE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					weights = TRUE
			)
			
			callNextMethod(.Object, label="Multivariate Adaptive Regression Splines", pack="mda", desc=desc)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="regr.mars", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="missing" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			targetcol <- which(names(.data) == .targetvar)
			mars(x = as.matrix(.data[,-targetcol]), y = as.vector(.data[,targetcol]), ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "regr.mars", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "missing" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			predict(.model["learner.model"], newdata=.newdata)
		}
)



