#' Set hyperparameters of learner object. 
#' 
#' @param learner [\code{\linkS4class{learner}}]\cr 
#'        Learner object.   
#' @param ... [any] \cr
#'        Optional named (hyper)parameters. Alternatively, you can pass via the "par.vals" argument.
#' @param par.vals [list] \cr
#'       Optional list of named (hyper)parameters. Alternatively, you can pass via the ... argument.
#' 		    
#' @return \code{\linkS4class{learner}} with changed hyperparameters.
#' @exportMethod set.hyper.pars
#' @title Set hyperparamters of learner object.
#' @rdname set.hyper.pars 

setGeneric(
    name = "set.hyper.pars",
    def = function(learner, ..., par.vals) {
      if (missing(par.vals))
        par.vals = list()
      par.vals = insert(par.vals, list(...))
      standardGeneric("set.hyper.pars")
    }
)

#' @rdname set.hyper.pars 
setMethod(
    f = "set.hyper.pars",
    
    signature = signature(
        learner="learner", 
        par.vals="list" 
    ),
    
    def = function(learner, ..., par.vals) {
      ns = names(par.vals)
      pds = learner["par.descs"]
      for (i in seq(length=length(par.vals))) {
        n = ns[i]
        p = par.vals[[i]]
        pd = pds[[n]]
        if (is.null(pd)) {
          # no description: stop warn or quiet
          msg = paste(class(learner), ": Setting par ", n, " without description!", sep="")
          if (.mlr.local$errorhandler.setup$on.par.without.desc == "stop")
            stop(msg)
          if (.mlr.local$errorhandler.setup$on.par.without.desc == "warn")
            warning(msg)
          pd = new("par.desc.unknown", par.name=n, default=p)
          learner@par.descs = append(learner@par.descs, pd)
          learner@par.vals[[n]] = p
        } else {
          # normal case: description there. now check for correct value
          if (is(pd, "par.desc.log")) {
            if(!is.logical(p) || length(p) != 1)
              stop(class(learner), ": Par ", n, " has to be a single boolean value!")
          } else if (is(pd, "par.desc.num")){
            if(!is.numeric(p) || length(p) != 1)
              stop(class(learner), ": Par ", n, " has to be a single numerical value!")
            if (pd["data.type"] == "integer")
              p = as.integer(p)
            if (p < pd["lower"] || p > pd["upper"])
              stop(class(learner), ": Par ", n, " has to be between bounds ", pd["lower"], " and ", pd["upper"], "!")
          } else if (is(pd, "par.desc.disc")){
            vals = pd["vals"]
            # we allow the usage of name for complex values
            if (is.character(p) && p %in% names(vals))
              p = vals[[p]]
            y = sapply(vals, function(x) isTRUE(all.equal(x, p)))
            if (!(any(y)))
              stop(class(learner), ": Given value of par. ", n, " has to be among allowed values!")
          }
          learner@par.vals[[n]] = p
        }
      }
      return(learner)
    } 
)


