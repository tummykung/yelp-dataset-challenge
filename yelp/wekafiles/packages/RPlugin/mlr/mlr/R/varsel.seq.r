
# todo maximize!!!!!!!!!!!!!!!!!!!!!!
# todo maximize!!!!!!!!!!!!!!!!!!!!!!
# todo maximize!!!!!!!!!!!!!!!!!!!!!!
# todo maximize!!!!!!!!!!!!!!!!!!!!!!
# todo maximize!!!!!!!!!!!!!!!!!!!!!!
# todo maximize!!!!!!!!!!!!!!!!!!!!!!
# be sel best usw.



# todo: maxit, max.vars
varsel.seq = function(learner, task, resampling, measures, aggr, control) {
	
	seq.step = function(forward, state, gen.new.states, compare) {
		not.used = setdiff(all.vars, state$par)
		new.states = gen.new.states(state$par, not.used)
		if (length(new.states) == 0)
			return(NULL)
		vals = list()
		
		event = ifelse(forward, "forward", "backward")
		
		es = eval.states.varsel(learner=learner, task=task, resampling=resampling, 
				measures=measures, aggr=aggr, control=control, pars=new.states, event=event)
		#print(unlist(vals))
		
		s = select.best.state(es, control)
		if (forward)
			thresh = control["alpha"]
		# if backward step and we have too many vars we do always go to the next best state with one less var.
		else
			thresh = ifelse(length(state$par) <= control["max.vars"], control["beta"], -Inf)
		if (!compare(state, s, control, measures, aggr, thresh)) {
			s = NULL
      changed = "<>"
    } else {
      # symmetric diff for changed feature
      changed = setdiff(union(state$par, s$par), intersect(state$par, s$par))
    } 

    logger.info(level="varsel", paste("varsel: forward=",forward, " features=", length(state$par), " perf=", round(get.perf(state), 3), " feat=", changed, sep=""))      
		path <<- add.path.els.varsel(path, es, s)
		return(list(path=path, state=s))
	}
	
	gen.new.states.sfs = function(vars, not.used) {
		new.states = list()
		for (i in seq(along=not.used)) {
			#cat(not.used[i], " ")
			new.states[[i]] = c(vars, not.used[i])
		}
		#cat("\n")
		return(new.states)
	}
	
	
	gen.new.states.sbs = function(vars, not.used) {
		new.states = list()
		for (i in seq(along=vars)) {
			new.states[[i]] = setdiff(vars, vars[i])
		}
		#cat("\n")
		return(new.states)
	}
	
	all.vars = task["input.names"]
	path = list()
	
	method = control["method"]
	
	start.vars = switch(method,
			sfs = character(0),
			sbs = all.vars,
			sffs = character(0),
			sfbs = all.vars,
			stop(paste("Unknown method:", method))
	) 
	
	gen.new.states = switch(method,
			sfs = gen.new.states.sfs,
			sbs = gen.new.states.sbs,
			sffs = gen.new.states.sfs,
			sfbs = gen.new.states.sbs,
			stop(paste("Unknown method:", method))
	) 
	
	state = eval.state.varsel(learner, task, resampling, measures, aggr, control, par=start.vars, event="start")
	
	path = add.path.varsel(path, state, accept=TRUE)		
	
	compare = compare.diff
	
	forward = (method %in% c("sfs", "sffs"))
	
	while (TRUE) {
		logger.debug("current:")
		logger.debug(state$par)
		#cat("forward:", forward, "\n")
		# if forward step and we habe enuff features: stop
		if (forward && control["max.vars"] <= length(state$par))
			break
		s = seq.step(forward, state, gen.new.states, compare)	
		#print(s$rp$measures["mean", "mmce"])
		if (is.null(s$state)) {
			break;
		} else {
			state = s$state
		}
		
		while (method %in% c("sffs", "sfbs")) {
			#cat("forward:", !forward, "\n")
			gns = switch(method,
					sffs = gen.new.states.sbs,
					sfbs = gen.new.states.sfs
			) 
			# if forward step and we habe enuff features: stop
			if (!forward && control["max.vars"] <= length(state$par))
				break
			s = seq.step(!forward, state, gns, compare)
			if (is.null(s$state)) {
				break;
			} else {
				state = s$state
			}
		}
	}
	new("opt.result", control=control, opt=make.path.el(state), path=path)
}





