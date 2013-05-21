
resample.fit.iter <- function(learner, task, rin, par.vals, vars, i, extract) {
	train.i = get.train.set(rin, i)
	ts = get.test.set(rin, i)
	test.i = ts$inds
	g = ts$group
	
	m = train(learner, task, subset=train.i, par.vals=par.vals, vars=vars)
	p = predict(m, task=task, subset=test.i, group=g)
	# faster for parallel
	ex = extract(m)
	return(list(pred=p, extracted=ex))	
}

eval.rf <- function(learner, task, resampling, measures, aggr, control, par) {

	if (is(control, "tune.control")) {
		par.vals = .mlr.scale.par(par, control)
		vars = task["input.names"]
	} else {
		par.vals = list()
		vars = par
	}
	# todo 
#	if (control["tune.threshold"]) 
#		type = "prob"
	
	rf = resample.fit(learner, task, resampling, par.vals=par.vals, vars=vars)
	th = as.numeric(NA)
	if (control["tune.threshold"]) { 
		thr = tune.threshold(rf, measures, aggr, task, minimize=control["minimize"], thresholds=control["thresholds"])
		rf = thr$pred
		th = thr$th
	}
	perf = performance(rf, measures=measures, aggr=aggr, task=task)
	list(perf=perf, th=th)
}

