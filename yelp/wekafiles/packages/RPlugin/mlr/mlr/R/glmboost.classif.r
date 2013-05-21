# todo: we could pass costs with extra loss function?

#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
		"classif.glmboost", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.glmboost"),
		def = function(.Object) {
			
			desc = new("learner.desc.classif",
					oneclass = FALSE,
					twoclass = TRUE,
					multiclass = FALSE,
					missings = FALSE,
					numerics = TRUE,
					factors = TRUE,
					characters = FALSE,
					probs = TRUE,
					decision = FALSE,
					weights = TRUE,
					costs = FALSE
			)
			# cannot pass the function Binomial without lopading the package in the super constructor...
			x = callNextMethod(.Object, label="glmboost", pack="mboost", desc=desc)
			par.descs = list(
					new("par.desc.disc", par.name="family", default="Binomial", vals=list(AdaExp=AdaExp(), Binomial=Binomial())),
					new("par.desc.num", par.name="mstop", default=100L, lower=1L),
					new("par.desc.num", par.name="nu", default=0.1, lower=0, upper=1),				
					new("par.desc.log", par.name="center", default=FALSE)
			)
			x@par.descs = par.descs
			set.hyper.pars(x, family="Binomial")
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.glmboost", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
		
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			xs = args.to.control(boost_control, c("mstop", "nu", "risk"), list(...))
			f = as.formula(paste(.targetvar, "~."))
			args = c(list(f, data=.data, weights=.weights, control=xs$control), xs$args)
			do.call(glmboost, args)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.glmboost", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			type = ifelse(.type=="response", "class", "response")
			p = predict(.model["learner.model"], newdata=.newdata, type=type, ...)
			if (.type == "prob") {
				p = p[,1]
				y = matrix(0, ncol=2, nrow=nrow(.newdata))
				colnames(y) <- .model["class.levels"]
				y[,1] = p
				y[,2] = 1-p
				return(y)
			} else {
				return(p)
			}
		}
)	





