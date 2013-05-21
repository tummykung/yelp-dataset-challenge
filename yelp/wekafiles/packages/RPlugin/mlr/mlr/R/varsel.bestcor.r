
varsel.bestcor = function(learner, task, resampling, measures, aggr, control=sequential.control()) {
	all.vars = task["input.names"]
	path = list()
	
	start.vars = character(0)	
	state = eval.state.varsel(learner, task, resampling, measures, aggr, par=start.vars, event="start")
	
	path = add.path.varsel(path, state, T)		
	data = na.omit(task["data"])
	not.used = all.vars
	cors = abs(cor(data[, all.vars], data[, task["target"]])) 
	o = order(cors, decreasing=TRUE)
	not.used = not.used[o]
	#print(cors)
	while (TRUE) {
		#print("current:")
		#print(state$par)
		found = FALSE
		for (i in seq(along.with=not.used)) {
			#print(.mlr.vareval)
			v = not.used[i]
			#cat(v, "\n")
			new.vars = c(state$par, v)
			if (get(".mlr.vareval", envir= .GlobalEnv) >= control$maxit)
				break
			s = eval.state.varsel(learner, task, resampling, measures, aggr, par=new.vars, "forward")
			cc = compare.diff(state, s, control, measures, aggr, control$alpha)
			path = add.path.varsel(path, s, accept=cc)		
			if (cc) {
				state = s
				not.used = not.used[-i]
				found = TRUE
				break
			}
		}
		if (!found)
			break
	}
	new("opt.result", control=control, opt=make.path.el(state), path=path)
}





