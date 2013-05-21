

warn.wrapper = function(x, myfun, arg.names) {
	assign(".mlr.slave.warnings", character(0), envir = .GlobalEnv)
	
	withCallingHandlers({
				args = mget(arg.names, env=.GlobalEnv)
				args[[length(args)+1]] = x
				y = do.call(myfun, args)
			}, 
			warning = function(w) {
				sws = get(".mlr.slave.warnings", envir = .GlobalEnv) 
				assign(".mlr.slave.warnings", c(sws, w), envir = .GlobalEnv)
			}
	)
	sws = get(".mlr.slave.warnings", envir = .GlobalEnv) 
	if (length(sws) > 0)
		attr(y, ".mlr.slave.warnings") = sws
	return(y)
}


mylapply <- function(xs, f, from, ...) {
	ps = .mlr.local$parallel.setup
	if (ps$mode == "local" || ps$level != from) {
		y = lapply(xs, f, ...)
	} else {
		args = list(...)
		ns = names(args)
		
		for (i in seq(length(ns))) {
			export(ns[i], args[[i]])
		}
		
		if (ps$mode %in% c("sfCluster", "snowfall")){
			y = sfClusterApplyLB(x=xs, fun=warn.wrapper, myfun=f, arg.names=ns)		
		} else if (ps$mode == "multicore") {
			# todo check warnings
			y = mclapply(xs, f, ..., mc.cores=ps$cpus)
		} else {
			stop("Unknown parallel model: ", ps$mode)
		}
	}
	
	if (.mlr.local$logger.setup$global.level == "debug" && .mlr.local$logger.setup$sublevel == "parallel") {
		sizes = sapply(y, object.size)
		logger.debug(level="parallel", "mylapply returned sizes:", range(sizes))
	}
	
	if (length(y) > 0) {
		for (i in 1:length(y)) {
			x = y[[i]]
			if (is(x, "try-error")) {
				stop(paste("On slave:", x))
			}
			ws = attr(x, ".mlr.slave.warnings")
			if (!is.null(ws)) {
				warning(paste("On slave:", ws))
				attr(y[[i]], ".mlr.slave.warnings") = NULL
			}
		}
	}
	return(y)
}



