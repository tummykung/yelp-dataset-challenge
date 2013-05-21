#' @include task.learn.r
#' @include opt.wrapper.r

benchmark_par = function(ind, learners, tasks, resampling, measures, conf.mat, models, paths) {
	i = ind[1]
	j = ind[2]
  .mlr.benchmark(learners[[i]], tasks[[j]], resampling, measures, conf.mat, models, paths)
}


#' Helper fucntion for bench.exp. Internal use. 
#' 
#' @seealso \code{\link{bench.exp}} 
#' @export 
#' @title Helper fucntion for bench.exp. Internal use.

.mlr.benchmark = function(learner, task, resampling, measures, conf.mat, models, paths) {
	
	if (is.character(learner)) {
		learner = make.learner(learner)
	}
	
	logger.info(paste("bench.exp: task=", task["id"], " learner=", learner["id"]))
	
	if (missing(measures))
		measures = default.measures(task)
	measures = make.measures(measures)
	
	if (is(learner, "opt.wrapper")) {
		learner@control@path = paths
		if (models) 
			extract = function(x) list(model=x, or=x["opt.result"])
		else 
			extract = function(x) list(or=x["opt.result"])
	} else {
		if (models)	
			extract = function(x) {list(model=x)} 
		else 
			extract = function(x) {}
	}

	
	rr = resample.fit(learner, task, resampling, extract=extract)
	result = data.frame(matrix(nrow=resampling["iters"]+1, ncol=0))
	ex = rr@extracted
	
	
	rp = performance(rr, measures=measures, aggr=list("combine"), task=task)
	cm = NULL
	if (is(task, "classif.task") && conf.mat)			
		cm = conf.matrix(rr)
  # throw away info about measures on groups here, this is only for combine anyway 
  if (resampling["has.groups"])
    ms = rp$aggr.group
  else
    ms = rp$measures
  # add in combine because we cannot get that later if we throw away preds
  ms = rbind(ms, cbind(iter=0, rp$aggr))
	result = cbind(result, ms)
	rownames(result) = rownames(ms)
	mods = NULL
	if (models) 
		mods = lapply(rr@extracted, function(x) x$model)
	ors = NULL
	ors = lapply(rr@extracted, function(x) x$or)
	return(list(result=result, conf.mat=cm, resample.fit=rr, models=mods, ors=ors))
}

