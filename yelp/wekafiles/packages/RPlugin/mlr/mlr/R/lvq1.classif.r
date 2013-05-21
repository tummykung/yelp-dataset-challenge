#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
		"classif.lvq1", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.lvq1"),
		def = function(.Object) {
			
			desc = new("learner.desc.classif",
					oneclass = FALSE,
					twoclass = TRUE,
					multiclass = TRUE,
					missings = FALSE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					probs = FALSE,
					decision = FALSE,
					weights = FALSE,
					costs = FALSE
			)
			
			callNextMethod(.Object, label="lvq1", pack="class", desc=desc)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.lvq1", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			inputs = setdiff(colnames(.data), .targetvar)
			
			cdbk.args = insert(list(), list(...), c("size", "k", "prior"))
			cdbk.args$x = .data[,inputs] 
			cdbk.args$cl = .data[,.targetvar] 
			codebk = do.call(lvqinit, cdbk.args)  

			lvq.args = insert(list(), list(...), c("niter", "alpha"))
			lvq.args$x = .data[,inputs] 
			lvq.args$cl = .data[,.targetvar] 
			lvq.args$codebk = codebk 
			do.call(lvq1, lvq.args)  
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.lvq1", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			lvqtest(.model["learner.model"], test=.newdata, ...)
		}
)	






