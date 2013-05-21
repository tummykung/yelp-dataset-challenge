#' @include learnerR.r
roxygen()


setClass(
  # name lm is sealed
  "regr.rsm", 
  contains = c("rlearner.regr")
)


setMethod(
  f = "initialize",
  signature = signature("regr.rsm"),
  def = function(.Object) {
    
    desc = new("learner.desc.regr",
      missings = FALSE,
      numerics = TRUE,
      factors = FALSE,
      characters = FALSE,
      weights = FALSE
    )
    
    par.descs = list(      
      new("par.desc.disc", par.name="modelfun", default="FO", vals=c("FO", "TWI", "SO"), flags=list(pass.default=TRUE))
    )
    
    callNextMethod(.Object, label="rsm", pack="rsm", desc=desc, par.descs=par.descs)
  }
)

#' @rdname train.learner

setMethod(
  f = "train.learner",
  signature = signature(
    .learner="regr.rsm", 
    .targetvar="character", 
    .data="data.frame", 
    .data.desc="data.desc", 
    .task.desc="task.desc", 
    .weights="numeric", 
    .costs="missing" 
  ),
  
  def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, ...) {
    mf = list(...)$modelfun
    vs = setdiff(colnames(.data), .targetvar)
    vs2 = paste(vs, collapse=",")
    g = function(x) paste(x, "(", vs2, ")", sep="") 
    mf = switch(mf,
      FO = g("FO"),
      TWI = paste(g("TWI"), "+", g("FO")),
      SO = g("SO"),
      stop("Unknown modelfun: ", mf)
    )
    f = as.formula(paste(.targetvar, "~", mf))
    myargs = list(f, .data)
    # strange behaviour in rsm forces us to use do.call...
    do.call(rsm, myargs)
  }
)

#' @rdname pred.learner

setMethod(
  f = "pred.learner",
  signature = signature(
    .learner = "regr.rsm", 
    .model = "wrapped.model", 
    .newdata = "data.frame", 
    .type = "missing" 
  ),
  
  def = function(.learner, .model, .newdata, ...) {
    as.numeric(predict(.model["learner.model"], newdata=.newdata, ...))
  }
)	





