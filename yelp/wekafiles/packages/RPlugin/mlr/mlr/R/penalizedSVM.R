#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()



setClass(
		"classif.lpsvm", 
		contains = c("rlearner.classif")
)

setMethod(
		f = "initialize",
		signature = signature("classif.lpsvm"),
		def = function(.Object) {
			
			desc = new("learner.desc.classif",
					oneclass = FALSE,
					twoclass = TRUE,
					multiclass = TRUE,
					missings = FALSE,
					numerics = TRUE,
					factors = TRUE,
					characters = TRUE,
					probs = TRUE, 
					decision = FALSE,
					weights = TRUE,	
					costs = FALSE 
			)
			
			callNextMethod(.Object, label="lpsvm", pack="penalizedSVM", desc=desc)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.lpsvm", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			f = as.formula(paste(.targetvar, "~."))
			kpar = list()
			args = list(...)
			args.names <- names(args)
		
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.lpsvm", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			.type <- ifelse(.type=="response", "response", "probabilities")
			predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
		}
)	


