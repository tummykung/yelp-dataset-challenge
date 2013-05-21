

tune.cmaes = function(learner, task, resampling, measures, aggr, control) {
  require.packs("cmaes", "tune.cmaes")
	path = list()
	
	g = function(p) {
		p2 = as.list(p)
		names(p2) = ns
		es = eval.state.tune(learner, task, resampling, measures, aggr, control, p2, "optim")
		path <<- add.path.tune(path, es, accept=TRUE)		
		perf = get.perf(es)
		logger.info(level="tune", paste(ns, "=", p), ":", perf)
		return(perf)
	}

  g2 = function(p) {
    p2 = as.list(as.data.frame(p))
    p2 = lapply(p2, function(x) {x=as.list(x);names(x)=ns;x})
    es = eval.states.tune(learner, task, resampling, measures, aggr, control, p2, "optim")
    path <<- add.path.els.tune(path=path, ess=es, best=NULL)
    perf = sapply(es, get.perf)
    # cma es does not like NAs which might be produced if the learner gets values which result in a degenerated model
    if (control["minimize"])
      perf[is.na(perf)] = Inf
    else
      perf[is.na(perf)] = -Inf
    return(perf)
  }
  
	args = control@extra.args
	
	ns = names(control["start"])
	start = as.numeric(control["start"])
	
  if (.mlr.local$parallel.setup$mode != "local" && .mlr.local$parallel.setup$level == "tune") {
    g=g2
    args$vectorized=TRUE    
  }  

  or = cma_es(par=start, fn=g, lower=control["lower"], upper=control["upper"], control=args)
	par = as.list(or$par)
	names(par) = ns
	opt = get.path.el(path, par)
	new("opt.result", control=control, opt=opt, path=path)
}
