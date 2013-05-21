#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
		"classif.nnet", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.nnet"),
		def = function(.Object) {
			
			desc = new("learner.desc.classif",
					oneclass = FALSE,
					twoclass = TRUE,
					multiclass = TRUE,
					missings = FALSE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					probs = TRUE,
					decision = FALSE,
					weights = TRUE,
					costs = FALSE
			)

			par.descs = list(
				new("par.desc.num", par.name="size", default=3L, lower=0, flags=list(pass.default=TRUE)),
                new("par.desc.num", par.name="maxit", default=100L, lower=1L)
            )
      			
			callNextMethod(.Object, label="NNet", pack="nnet", desc=desc, par.descs=par.descs)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.nnet", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			f = as.formula(paste(.targetvar, "~."))
			nnet(f, data=.data, weights=.weights, ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.nnet", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			.type = switch(.type, response="class", prob="raw")
			p = predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
			if (.type == "class")
				return(as.factor(p))
			else {
				if (.model["class.nr"] == 2) {
          y <- cbind(p, 1-p) 
					colnames(y) = .model["class.levels"]
					return(y)
				} else
					return(p)	
			}
		}
)	
