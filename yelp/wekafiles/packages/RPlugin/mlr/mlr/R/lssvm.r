#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
		"classif.lssvm", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.lssvm"),
		def = function(.Object) {
			
			desc = new("learner.desc.classif",
					oneclass = FALSE,
					twoclass = TRUE,
					multiclass = TRUE,
					missings = FALSE,
					numerics = TRUE,
					factors = TRUE,
					characters = TRUE,
					probs = FALSE,
					decision = TRUE,
					weights = FALSE,	
					costs = FALSE 
			)
			
			callNextMethod(.Object, label="lssvm", pack="kernlab", desc=desc)
		}
)


#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.lssvm", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		# todo custom kernel. freezes? check mailing list
		# todo unify cla + regr, test all sigma stuff
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			
#			# there's a strange behaviour in r semantics here wgich forces this, see do.call and the comment about substitute
#			if (!is.null(args$kernel) && is.function(args$kernel) && !is(args$kernel,"kernel")) {
#				args$kernel = do.call(args$kernel, kpar)	
#			} 
			
			xs = args.to.control(list, c("degree", "offset", "scale", "sigma", "order", "length", "lambda", "normalized"), list(...))
			f = as.formula(paste(.targetvar, "~."))
			if (length(xs$control) > 0)
				args = c(list(f, data=.data, fit=FALSE, kpar=xs$control), xs$args)
			else
				args = c(list(f, data=.data, fit=FALSE), xs$args)
			do.call(lssvm, args)
			
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.lssvm", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			.type <- switch(.type, decision="decision", "response")
			predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
		}
)	

