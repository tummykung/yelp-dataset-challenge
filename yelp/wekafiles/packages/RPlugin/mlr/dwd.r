#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
		"classif.DWD", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.DWD"),
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
			
			callNextMethod(.Object, label="DWD", pack="DWD", desc=desc)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.DWD", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			f = as.formula(paste(.targetvar, "~."))
			kdwd(f, data=.data, ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.DWD", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			#.type <- ifelse(.type=="response", "class", "raw")
			#predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
			if (.type=="response") {
				as.factor(predict(.model["learner.model"], newdata=.newdata))
			} else {
				response <- as.factor(predict(.model["learner.model"], newdata=.newdata))
				probMat <- matrix(0, length(response), length(levels(response)))
				colnames(probMat) <- levels(response)
				for (level in levels(response)) {
					probMat[,level] <- (response==level)*1
				}
				probMat
			}
		}
)	



