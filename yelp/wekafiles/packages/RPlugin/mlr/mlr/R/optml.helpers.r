# todo: the global eval vars are very bad, think about parallel! 


#state: list(vars, rp) 

# get a single perf value for a state: first measure, aggregated by first aggr function
get.perf = function(state) {
	state$rp$aggr[1,1]
}

# get all aggr. perf values for a state in a vector
flat.perfs = function(state) {
	mm = state$rp$aggr
	ns = expand.grid(rownames(mm), colnames(mm))
	ns = apply(ns, 1, function(i) paste(i[1], i[2], sep="."))
	mm =  as.numeric(as.matrix(mm))
	names(mm) = ns 
	return(mm)	
}

make.path.el = function(es, accept=0) {
	list(par = es$par, threshold=es$threshold, perf = flat.perfs(es), evals=es$evals, event=es$event, accept=accept)
}

make.es = function(par, rp, evals, event) {
	return(list(par=par, rp=rp$perf, threshold=rp$th, evals=evals, event=event))
}

add.path = function(global.eval.var, path, es, accept) {
	a = ifelse(accept, get(global.eval.var, envir=.GlobalEnv), -1)
	pe = make.path.el(es, accept = a)
	path[[length(path) + 1]] = pe
	return(path)
} 

# best = NULL means no acceptable new element was found
add.path.els = function(global.eval.var, path, ess, best) {
	for (i in 1:length(ess)) {
		es = ess[[i]]
		path = add.path(global.eval.var, path, es, !is.null(best$par) && setequal(es$par, best$par))
	}
	return(path)
} 


eval.state = function(global.eval.var, learner, task, resampling, measures, aggr, control, par, event) {
	rp = eval.rf(learner=learner, task=task, resampling=resampling,  
			measures=measures, aggr=aggr, control=control, par=par)
	evals = get(global.eval.var, envir=.GlobalEnv)+1
	assign(global.eval.var, evals, envir=.GlobalEnv)
	make.es(par=par, rp=rp, evals=evals, event=event)
}

# evals a set of var-lists and return the corresponding states
eval.states = function(global.eval.var, eval.fun, learner, task, resampling, measures, aggr, control, pars, event) {
	rps = eval.fun(learner=learner, task=task, resampling=resampling,  
			measures=measures, aggr=aggr, control=control, pars=pars)
	evals = get(global.eval.var, envir=.GlobalEnv)
	evals2 = evals + length(pars)
	assign(global.eval.var, evals2, envir=.GlobalEnv)
	f = function(x1,x2,x3,x4) make.es(par=x1, rp=x2, evals=x3, event=x4) 
	Map(f, pars, rps, (evals+1):evals2, event)
}


# compare 2 states.  
# TRUE : state2 is significantly better than state1  
# compare = function(state1, state2, control, measures, aggr, threshold) 


# use the difference in performance   
compare.diff = function(state1, state2, control, measures, aggr, threshold) {
	m1 = get.perf(state1)
	m2 = get.perf(state2)
	d = ifelse(control["minimize"], 1, -1) * (m1 - m2)
	(d > threshold)	
}



# select the best state from a list by using get.perf
select.best.state = function(states, control) {
	perfs = sapply(states, get.perf)
	if (control["minimize"])
		i = which.min(perfs)
	else 
		i = which.max(perfs)
  # all perfs can be NA if all learners failed, then select randomly
  if (all(is.na(perfs))) {
    warning("All evaluated states had NA performance, selecting 1 randomly!")
    i = sample(1:length(perfs), 1)
  }
	return(states[[i]])
}

# retrieve el from path
get.path.el = function(path, par) {
	Find(function(x) identical(x$par, par), path)
} 


