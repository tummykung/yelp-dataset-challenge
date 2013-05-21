



#export.resample.fit = function(learner, task, resample.instance, parset, vars, type, extract) {
##	if (!is.null(parent.frame()$caller) && !parent.frame()$caller == "tune") {
#		
#	export(".mlr.learner", learner)
#	export(".mlr.task", task)
#	export(".mlr.rin", resample.instance)
#	export(".mlr.parset", parset)
#	export(".mlr.vars", vars)
#	export(".mlr.type", type)
#	export(".mlr.extract", extract)
#}


export.tune <- function(learner, task, loss, scale) {
	export(".mlr.learner", learner)
	export(".mlr.task", task)
	export(".mlr.loss", loss)
	export(".mlr.scale", scale)
}


export <- function(name, obj) {
	doit = TRUE
	# multicore does not require to export because mem is duplicated after fork (still copy-on-write)
	if (.mlr.local$parallel.setup$mode != "local" && .mlr.local$parallel.setup$mode != "multicore") {
		if (is(obj, "learn.task")) {
			hash = digest(list(name, obj))
			if (exists(hash, envir=.mlr.export)) 
				doit = FALSE
			else {
				assign(hash, TRUE, env=.mlr.export)
			}
		}
		if (doit) {
			sfClusterCall(assign, name, obj, env=globalenv())
		}
	}
}