eval.varsets = function(learner, task, resampling, measures, aggr, control, pars) {
	rps = mylapply(xs=pars, from="varsel", f=eval.rf, 
			learner=learner, task=task, resampling=resampling, measures=measures, aggr=aggr, control=control)
	return(rps)
}

# evals a set of var-lists and return the corresponding states
eval.states.varsel = function(learner, task, resampling, measures, aggr, control, pars, event) {
	eval.states(".mlr.vareval", eval.varsets, learner=learner, task=task, resampling=resampling, 
			measures=measures, aggr=aggr, control=control, pars=pars, event=event)
}

eval.state.varsel = function(learner, task, resampling, measures, aggr, control, par, event) {
	eval.state(".mlr.vareval", learner, task, resampling, measures, aggr, control, par, event)
}


add.path.varsel = function(path, es, accept) {
	add.path(".mlr.vareval", path, es, accept)
} 

add.path.els.varsel = function(path, ess, best) {
	add.path.els(".mlr.vareval", path, ess, best)	
} 
