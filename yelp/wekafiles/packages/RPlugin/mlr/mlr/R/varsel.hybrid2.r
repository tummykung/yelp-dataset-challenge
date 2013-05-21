
varsel.hybrid2 = function(learner, task, resampling, measures, aggr, control=sequential.control()) {
	
	path = list()
	all.vars = task["input.names"]
	data = na.omit(task["data"])
	cors.y = abs(cor(data[,all.vars], data[,task["target"]]))[,1]
	# NA can occur if var. is constant, set cor to 0 then, var useless
	cors.y[is.na(cors.y)] = 0
	m = length(all.vars) 
	flip.rate = control$epsilon
	p01 = control$delta
	prob.greedy = control$alpha
	# look back for adaption of flip.rate (step.size)
	win.size = 10
	
	start = all.vars[as.logical(rbinom(m, 1, 0.5))]
	#cat("start:", start, "\n")
	state = eval.state.varsel(learner, task, resampling, measures, aggr, par=start, "start")
	path = add.path.varsel(path, state, T)		
	#print(get.perf(state))
	
	# big loop for mut + local
	mut.succ = c()
	while (get(".mlr.vareval", envir= .GlobalEnv) < control$maxit) {
		
		# mutate til successful
		vs.bin = all.vars %in% state$par
		#print(vs.bin)
		while (get(".mlr.vareval", envir= .GlobalEnv) < control$maxit) {
			greedy = (rbinom(1, 1, prob.greedy) == 1)
			if (greedy) {
				probs = abs(as.numeric(vs.bin) - cors.y)
			} else {
				# we look back win.size mutations and maybe adapt the step.size
				if (length(mut.succ) > win.size) {
					mean.succ = mean(mut.succ[(length(mut.succ)-win.size+1):length(mut.succ)])
					if (mean.succ > 0.3)
						flip.rate = flip.rate/1.2
					if (mean.succ < 0.1)
						flip.rate = flip.rate*1.2
				}
				if (flip.rate > m/2)
					flip.rate = m/2
				probs = abs(as.numeric(vs.bin) - p01) * flip.rate / m
			}
			mut = as.logical(rbinom(m, 1, prob=probs))
			new.bin = (vs.bin != mut)
			new.vars = all.vars[new.bin]
			op = ifelse(greedy, "mut.greedy", "mut.normal")
			new.state = eval.state.varsel(learner, task, resampling, measures, aggr, par=new.vars, op)
			cc = compare.diff(state, new.state, control, measures, aggr, threshold=control$gamma)	&& 
					(length(new.state$par) > 0)
			path = add.path.varsel(path, new.state, cc)
			if (!greedy)
				mut.succ = c(mut.succ, as.numeric(cc))
			if (cc) {
				state=new.state
				break
			}
		}
	} # end big loop	
	new("opt.result", control=control, opt=make.path.el(state), path=path) 
}	




