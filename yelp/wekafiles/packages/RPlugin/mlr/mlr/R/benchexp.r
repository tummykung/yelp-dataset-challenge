#' Complete benchmark experiment to compare different learning algorithms 
#' across one or more tasks w.r.t. a given resampling strategy.  
#' Experiments are paired, meaning always the same training / test sets are used for the different learners.  

#' @param learners [string | \code{\linkS4class{learner}} | list of the previous two] \cr
#' 		  Defines the learning algorithms which should be compared.
#' @param tasks [\code{\link{learn.task}} | list of the previous] \cr
#'        Defines the tasks.
#' @param resampling [resampling desc | resampling instance | list of the previous two] \cr
#'        Defines the resampling strategies for the tasks.
#' @param measures [see \code{\link{measures}}]
#'        Performance measures. 
#' @param conf.mats [logical] \cr
#'        Should confusion matrices be stored?
#'        Default is TRUE.
#' 		  Ignored for regression.	 
#' @param predictions [logical] \cr
#'        Should all predictions be stored?
#'        Default is FALSE.
#' @param models [logical] \cr
#'        Should all fitted models be stored?
#'        Default is FALSE.
#' @param paths [logical] \cr
#'        Should the optimization paths be stored?
#'        Default is FALSE.
#' @return \code{\linkS4class{bench.result}}.
#' 
#' @usage bench.exp(learners, tasks, resampling, measures, conf.mats=TRUE, predictions=FALSE, models=FALSE, paths=FALSE)
#' 
#' @note You can also get automatic, internal tuning by using \code{\link{make.tune.wrapper}} with your learner. 
#' 
#' @seealso \code{\link{make.tune.wrapper}} 
#' @export 
#' @aliases bench.exp 
#' @title Benchmark experiment for multiple learners and tasks. 


# todo: check unique ids
bench.exp <- function(learners, tasks, resampling, measures,  
		conf.mats=TRUE, predictions=FALSE, models=FALSE, paths=FALSE)  {
	
	if (!is.list(learners) && length(learners) == 1) {
		learners = list(learners)
	}
	learners = as.list(learners)
	n = length(learners)
	if (n == 0)
		stop("No learners were passed!")
	check.list.type(learners, c("character", "learner"))
	learners = lapply(learners, function(x) if (is.character(x)) make.learner(x) else x)
  ids = sapply(learners, function(x) x["id"])
	if (any(duplicated(ids)))
		stop("Learners need unique ids!")
	
	if (!is.list(tasks)) {
		tasks = list(tasks)
	}
	if (length(tasks) == 0)
		stop("No tasks were passed!")
	check.list.type(tasks, "learn.task")
  ids = sapply(tasks, function(x) x["id"])
  if (any(duplicated(ids)))
    stop("Tasks need unique ids!")
  
	if (missing(measures))
		measures = default.measures(tasks[[1]])
	measures = make.measures(measures)
	
	
	
	#bs = array(-1, nrow=resampling["iters"], ncol=n)
	## add dim for every loss ?? hmm, those are not always the same size...
	if (length(tasks) > 1 && is(resampling, "resample.instance")) {
		stop("Cannot pass a resample.instance with more than 1 task. Use a resample.desc!")
	}
	dims = c(resampling["iters"]+1, n, length(measures))
	bs = list()
	
	learner.names = character()
	task.names = sapply(tasks, function(x) x["id"])	
	resamplings = list()
	tds = dds = rfs = cms = mods = list()
	ors = list()
	
	
	inds = as.matrix(expand.grid(1:length(learners), 1:length(tasks)))
	inds = lapply(1:nrow(inds), function(i) inds[i,])
	results = mylapply(inds, benchmark_par, from="bench", learners=learners, tasks=tasks, resampling=resampling,
			measures=measures, conf.mat=conf.mats, models=models, paths=paths)
	
	counter = 1
	for (j in 1:length(tasks)) {
		bs[[j]] = array(0, dim = dims)		
		task = tasks[[j]]
		rfs[[j]] = list()
		cms[[j]] = list()
		mods[[j]] = list()
		ors[[j]] = list()
		if (is(resampling, "resample.desc")) {
			resamplings[[j]] = make.res.instance(resampling, task=task)
		} else {
			resamplings[[j]] = resampling
		}		
		tds[[j]] = task@task.desc
		dds[[j]] = task@data.desc
		for (i in 1:length(learners)) {
			wl = learners[[i]]
			learner.names[i] = wl["id"]
			bm = results[[counter]]
			counter = counter+1
			rr = bm$result
			rf = bm$resample.fit
			# remove tune perf
			rr = rr[, names(measures)]
			bs[[j]][,i,] = as.matrix(rr)
			
			if(predictions)	rfs[[j]][[i]] = rf else	rfs[[j]][i] = list(NULL)
			if(is(task, "classif.task") && conf.mats) cms[[j]][[i]] = bm$conf else cms[[j]][i] = list(NULL)
			if(models)	mods[[j]][[i]] = bm$models else	mods[[j]][i] = list(NULL)
			if(is(wl, "opt.wrapper")) ors[[j]][[i]] = bm$ors else ors[[j]][i] = list(NULL)
		}
		dimnames(bs[[j]]) = list(c(1:resampling["iters"], "combine"), learner.names, names(measures))
		
		names(rfs[[j]]) = learner.names
		names(cms[[j]]) = learner.names
		names(mods[[j]]) = learner.names
		names(ors[[j]]) = learner.names
	}
	names(bs) = task.names
	names(rfs) = task.names
	names(cms) = task.names
	names(mods) = task.names
	names(ors) = task.names
	return(new("bench.result", task.descs=tds, data.descs=dds, resamplings=resamplings, perf = bs, 
					predictions=rfs, conf.mats=cms, models=mods,
					opt.results = ors
			))
}
