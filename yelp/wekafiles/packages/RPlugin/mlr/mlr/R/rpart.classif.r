#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


# todo: parms has to be in hyperparamter list

setClass(
		"classif.rpart", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.rpart"),
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
					weights = TRUE,
					costs = TRUE
			)
			par.descs = list(
					new("par.desc.num", par.name="minsplit", default=20L, lower=1L),
					new("par.desc.num", par.name="minbucket", lower=1L),
					new("par.desc.num", par.name="cp", default=0.01, lower=0, upper=1),
					new("par.desc.num", par.name="maxcompete", default=4L, lower=0L, flags=list(optimize=FALSE)),
					new("par.desc.num", par.name="maxsurrogate", default=5L, lower=0L, flags=list(optimize=FALSE)),
					new("par.desc.disc", par.name="usesurrogate", default=2L, vals=0:2),
					new("par.desc.disc", par.name="surrogatestyle", default=0L, vals=0:1),
					new("par.desc.num", par.name="maxdepth", default=TRUE, lower=1, upper=30)
			)
			
			callNextMethod(.Object, label="RPart", pack="rpart", desc=desc, par.descs=par.descs)
		}
)

#' @rdname train.learner


setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.rpart", 
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
				rpart(f, data=.data, weights=.weights, parms=list(loss=.costs), ...)
			} else
				rpart(f, data=.data, weights=.weights, ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.rpart", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			.type = switch(.type, prob="prob", "class")
			predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
		}
)	





