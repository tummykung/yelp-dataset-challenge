#' @include learnerR.r
roxygen()
#' @include wrapped.model.r
roxygen()
#' @include train.learner.r
roxygen()
#' @include pred.learner.r
roxygen()


setClass(
    "regr.nnet", 
    contains = c("rlearner.regr")
)


setMethod(
    f = "initialize",
    signature = signature("regr.nnet"),
    def = function(.Object) {
      
      desc = new("learner.desc.regr",
          missings = FALSE,
          numerics = TRUE,
          factors = TRUE,
          characters = FALSE,
          weights = TRUE
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
        .learner="regr.nnet", 
        .targetvar="character", 
        .data="data.frame", 
        .data.desc="data.desc", 
        .task.desc="task.desc", 
        .weights="numeric", 
        .costs="missing" 
    ),
    
    def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {
      f = as.formula(paste(.targetvar, "~."))
      nnet(f, data=.data, weights=.weights, linout=T, ...)
    }
)

#' @rdname pred.learner

setMethod(
    f = "pred.learner",
    signature = signature(
        .learner = "regr.nnet", 
        .model = "wrapped.model", 
        .newdata = "data.frame", 
        .type = "missing" 
    ),
    
    def = function(.learner, .model, .newdata, .type, ...) {
      predict(.model["learner.model"], newdata=.newdata, ...)[,1]
    }
) 
