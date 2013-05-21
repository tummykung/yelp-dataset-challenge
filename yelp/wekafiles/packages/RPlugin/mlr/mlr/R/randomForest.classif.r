#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
		"classif.randomForest", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.randomForest"),
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
					weights = FALSE,
					costs = FALSE
			)
			
			par.descs = list(
					new("par.desc.num", par.name="ntree", default=500L, lower=1L),
					new("par.desc.num", par.name="mtry", lower=1),
					new("par.desc.log", par.name="replace", default=TRUE),
					new("par.desc.num", par.name="sampsize", lower=1L),
					new("par.desc.num", par.name="nodesize", default=1L, lower=1L),
					new("par.desc.num", par.name="maxnodes", lower=1L)
			)
			callNextMethod(.Object, label="RForest", pack="randomForest", desc=desc, par.descs=par.descs)
		}
)


#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.randomForest", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			f = as.formula(paste(.targetvar, "~."))
			randomForest(f, data=.data, ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.randomForest", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			.type <- ifelse(.type=="response", "response", "prob")
			predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
		}
)	









