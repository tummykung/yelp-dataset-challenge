#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()

setClass(
		"classif.rda", 
		contains = c("rlearner.classif")
)


setMethod(
		f = "initialize",
		signature = signature("classif.rda"),
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
					weights = FALSE,			
					costs = FALSE
			)
			par.descs = list(
					new("par.desc.num", par.name="lambda", default="missing", lower=0, upper=1),
					new("par.desc.num", par.name="gamma ", default="missing", lower=0, upper=1),
					new("par.desc.log", par.name="crossval", default=TRUE),
					new("par.desc.num", par.name="fold", default=10, lower=1),
					new("par.desc.num", par.name="train.fraction", default=0.5, lower=0, upper=1),
					new("par.desc.log", par.name="crossval", default=TRUE),
					new("par.desc.disc", par.name="schedule", default=1L, vals=1:2, requires=expression(simAnn==FALSE)),
					new("par.desc.num", par.name="T.start", default=0.1, lower=0, requires=expression(simAnn==TRUE)),
					new("par.desc.num", par.name="halflife", default=0.1, lower=0, requires=expression(simAnn==TRUE || schedule==1)),
					new("par.desc.num", par.name="zero.temp", default=0.01, lower=0, requires=expression(simAnn==TRUE || schedule==1)),
					new("par.desc.num", par.name="alpha", default=2, lower=1, requires=expression(simAnn==TRUE || schedule==2)),
					new("par.desc.num", par.name="K", default=100, lower=1, requires=expression(simAnn==TRUE || schedule==2)),
					
					new("par.desc.disc", par.name="kernel", default="triangular", 
							vals=list("rectangular", "triangular", "epanechnikov", "biweight", "triweight", "cos", "inv", "gaussian"))
			)
			
			callNextMethod(.Object, label="rda", pack="klaR", desc=desc, par.descs=par.descs)
		}
)

#' @rdname train.learner

setMethod(
		f = "train.learner",
		signature = signature(
				.learner="classif.rda", 
				.targetvar="character", 
				.data="data.frame", 
				.data.desc="data.desc", 
				.task.desc="task.desc", 
				.weights="numeric", 
				.costs="matrix" 
		),
    # todo: disable crossval. no, is done automaticall if pars are set.
		def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
			f = as.formula(paste(.targetvar, "~."))
			rda(f, data=.data, ...)
		}
)

#' @rdname pred.learner

setMethod(
		f = "pred.learner",
		signature = signature(
				.learner = "classif.rda", 
				.model = "wrapped.model", 
				.newdata = "data.frame", 
				.type = "character" 
		),
		
		def = function(.learner, .model, .newdata, .type, ...) {
			p <- predict(.model["learner.model"], newdata=.newdata, ...)
			if (.type=="response")
				return(p$class)
			else
				return(p$posterior)
			
		}
)	
