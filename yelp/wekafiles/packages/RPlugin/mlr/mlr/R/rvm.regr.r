#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
    "regr.rvm", 
    contains = c("rlearner.regr")
)


setMethod(
    f = "initialize",
    signature = signature("regr.rvm"),
    def = function(.Object) {
      
      desc = new("learner.desc.regr",
          missings = FALSE,
          numerics = TRUE,
          factors = TRUE,
          characters = TRUE,
          weights = FALSE  
      )
      
      # to do: stringdot pars and check order, scale and offset limits
      par.descs = list(
          new("par.desc.disc", par.name="kernel", default="rbfdot", 
              vals=c("vanilladot", "polydot", "rbfdot", "tanhdot", "laplacedot", "besseldot", "anovadot", "splinedot", "stringdot")),
          new("par.desc.num", par.name="tau", lower=0, default=0.01),
          new("par.desc.num", par.name="sigma",
              lower=0, requires=expression(kernel %in% c("rbfdot", "anovadot", "besseldot", "laplacedot"))),
          new("par.desc.num", par.name="degree", default=3L, lower=1L, 
              requires=expression(kernel %in% c("polydot", "anovadot", "besseldot"))),
          new("par.desc.num", par.name="scale", default=1, lower=0, 
              requires=expression(kernel %in% c("polydot", "tanhdot"))),
          new("par.desc.num", par.name="offset", default=1, 
              requires=expression(kernel %in% c("polydot", "tanhdot"))),
          new("par.desc.num", par.name="order", default=1L, 
              requires=expression(kernel == "besseldot")),
          new("par.desc.num", par.name="alpha", default=5L, lower=0L),
          new("par.desc.num", par.name="var", default=0.1, lower=0),
          new("par.desc.log", par.name="var.fix", default=FALSE),
          new("par.desc.num", par.name="iterations", default=100L, lower=0L),
          new("par.desc.num", par.name="tol", default=.Machine$double.eps, lower=0),
      		new("par.desc.num", par.name="minmaxdiff", default=0.001, lower=0)
      )
      
      callNextMethod(.Object, label="RVM", pack="kernlab", desc=desc)
    }
)


#' @rdname train.learner

setMethod(
    f = "train.learner",
    signature = signature(
        .learner="regr.rvm", 
        .targetvar="character", 
        .data="data.frame", 
        .data.desc="data.desc", 
        .task.desc="task.desc", 
        .weights="numeric", 
        .costs="missing" 
    ),
    
    # todo unify cla + regr, test all sigma stuff
    def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
      
      xs = args.to.control(list, c("degree", "offset", "scale", "sigma", "order", "length", "lambda", "normalized"), list(...))
      f = as.formula(paste(.targetvar, "~."))
      if (length(xs$control) > 0)
        args = c(list(f, data=.data, fit=FALSE, kpar=xs$control), xs$args)
      else
        args = c(list(f, data=.data, fit=FALSE), xs$args)
      do.call(rvm, args)
      
    }
)

#' @rdname pred.learner

setMethod(
    f = "pred.learner",
    signature = signature(
        .learner = "regr.rvm", 
        .model = "wrapped.model", 
        .newdata = "data.frame", 
        .type = "missing" 
    ),
    
    def = function(.learner, .model, .newdata, .type, ...) {
      predict(.model["learner.model"], newdata=.newdata, type=.type, ...)
    }
) 

