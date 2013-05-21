#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()

# checked props

setClass(
		"classif.J48", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.J48"),
		def = function(.Object) {
			
			desc = new("learner.desc.classif",
					oneclass = FALSE,
					twoclass = TRUE,
					multiclass = TRUE,
					missings = TRUE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					probs = TRUE,
					decision = FALSE,
					weights = FALSE,
					costs = FALSE
			)
			callNextMethod(.Object, label="J48", pack="RWeka", desc=desc)
		}
)

#' @rdname train.learner


setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.J48", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			f = as.formula(paste(.targetvar, "~."))
			ctrl = Weka_control(...)
			J48(f, data=.data, control=ctrl)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.J48", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			.type = switch(.type, prob="prob", "class")
			predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
		}
)	





