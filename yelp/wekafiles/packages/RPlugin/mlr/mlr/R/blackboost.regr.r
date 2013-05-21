#' @include learnerR.r
roxygen()

setClass(
		"regr.blackboost", 
		contains = c("rlearner.regr")
)


setMethod(
		f = "initialize",
		signature = signature("regr.blackboost"),
		def = function(.Object) {
			
			desc = new("learner.desc.regr",
					missings = FALSE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					weights = TRUE
			)
			
			callNextMethod(.Object, label="blackboost", pack="mboost", desc=desc)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="regr.blackboost", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="missing" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, ...) {
			xs = args.to.control(boost_control, c("mstop", "nu", "risk"), list(...))
			ys = args.to.control(ctree_control, c("teststat", "testtype", "mincriterion", "maxdepth"), xs$args)
			f = as.formula(paste(.targetvar, "~."))
			args = c(list(f, data=.data, weights=.weights, control=xs$control, tree_control=ys$control), ys$args)
			do.call(blackboost, args)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "regr.blackboost", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "missing" 
		),
		
		def = function(.learner, .model, .newdata, ...) {
			predict(.model["learner.model"], newdata=.newdata, ...)
		}
)	





