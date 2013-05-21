
varsel.random = function(learner, task, resampling, measures, aggr, control) {
	all.vars = task["input.names"]
	m = length(all.vars) 
	prob = control["prob"]
	
	states = list()
	for (i in 1:control["maxit"]) {
		vs = all.vars[as.logical(rbinom(m, 1, prob))]
		states[[i]] = vs
	}
	
	es = eval.states.varsel(learner, task, resampling, measures, aggr, control, states, "random")
	bs = select.best.state(es, control)
	
	path = add.path.els.varsel(list(), es, bs)
	new("opt.result", control=control, opt=make.path.el(bs), path=path)
}	