#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()

setClass(
		"classif.sda", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.sda"),
		def = function(.Object) {
			
			desc = new("learner.desc.classif",
					oneclass = FALSE,
					twoclass = TRUE,
					multiclass = TRUE,
					missings = FALSE,
					numerics = TRUE,
					factors = FALSE,
					characters = FALSE,
					probs = TRUE,
					decision = FALSE,
					weights = FALSE,			
					costs = FALSE
			)
			
			callNextMethod(.Object, label="Shrinkage Discriminant Analysis", pack="sda", desc=desc)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.sda", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			targetcol <- which(names(.data) == .targetvar)
			sda(Xtrain = as.matrix(.data[,-targetcol]), L = as.factor(.data[,targetcol]), ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.sda", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			p = predict(.model["learner.model"], as.matrix(.newdata))
			if(.type == "response")
				return(p$class)
			else
				return(p$posterior)
		}
)



