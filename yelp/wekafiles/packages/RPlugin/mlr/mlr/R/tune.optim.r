tune.optim = function(learner, task, resampling, measures, aggr, control) {
	start = control["start"]
	ns = names(start)
	
	path = list()
	
	g = function(p) {
		p2 = as.list(p)
		es = eval.state.tune(learner, task, resampling, measures, aggr, control, p2, "optim")
		path <<- add.path.tune(path, es, accept=TRUE)		
		perf = get.perf(es)
		logger.info(level="tune", paste(names(p), "=", p), ":", perf)
		return(perf)
	}
		
	args = control@extra.args
	method = args$method
	if(is.null(method)) 
		method = "Nelder-Mead"
	args$method = NULL
	
	if (method == "L-BFGS-B") {
		or = optim(par=start, f=g, method=method, lower=control["lower"], upper=control["upper"], control=args)
	} else 
		or = optim(par=start, f=g, method=method, control=args)
	par = as.list(or$par)
	opt = get.path.el(path, par)
	new("opt.result", control=control, opt=opt, path=path)
}
