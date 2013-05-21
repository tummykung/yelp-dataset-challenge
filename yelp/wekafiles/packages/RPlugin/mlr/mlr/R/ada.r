# Kosten bei allen gecheckt.

#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
		"classif.ada", 
		contains = c("rlearner.classif")
)



setMethod(
		f = "initialize",
		signature = signature("classif.ada"),
		def = function(.Object) {
			
			desc = new("learner.desc.classif",
					oneclass = FALSE,
					twoclass = TRUE,
					multiclass = FALSE,
					missings = TRUE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					probs = TRUE,
					decision = FALSE,
					weights = TRUE,
					costs = TRUE
			)
			
			callNextMethod(.Object, label="ada", pack="ada", desc=desc)
		}
)


#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.ada", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			f = as.formula(paste(.targetvar, "~."))
			if (!all(dim(.costs)) == 0) {
				lev = levels(.data[, .targetvar])
				.costs = .costs[lev, lev] 
				ada(f, data=.data, parms=list(loss=.costs), ...)
			} else
				ada(f, data=.data, ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.ada", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			.type <- ifelse(.type=="response", "vector", "prob")
			p = predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
			if (.type == "prob")
				colnames(p) = .model["class.levels"] 
			return(p)
		}
)	



