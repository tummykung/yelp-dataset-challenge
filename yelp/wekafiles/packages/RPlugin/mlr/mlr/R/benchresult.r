#' Container for the results of a benchmark experiment.
#' 
#' Getter. \cr
#' 
#' The "perf" getter is probably the most common one, it returns a list of 3 dim. arrays of performance values for every data set.
#' The dimension are: learners, resampling iterations and measures.
#' You can reduce the list or the contained array by using the optional arguments "task", "learner", "measure", "iter" and "aggr". 
#' "task" and "learners" must be set to a char vector of IDs repectively, "measure" to names of recorded performance measures in the experiment,
#' "iter" to an integer vector of selected resampling interations. The default for these is to select everything. 
#' "aggr" can be used to aggregate the results accross the resampling interations (see \code{\link{aggregations}}). 
#' The default is not to do any aggregation. You can also set "aggr" to "resampling" which does the default aggregation 
#' of the used resampling stratgegy.    
#' 'drop' is by default TRUE, which means that the structures are simplified as much as possible, if you don't want this set 'drop' to FALSE. 
#' 
#' The following getters all return list of lists of objects: prediction, conf.mat
#' The first list iterates the tasks, the second one the learners, both are named by respective IDs.
#' You can reduce these lists by using the optional arguments 'task' and 'learner'. 
#' 'drop' is by default TRUE, which means that the list structures are simplified as much as possible, if you don't want this set 'drop' to FALSE. 
#' 
#' The following getters all return list of lists of lists: opt.result, opt.par, opt.perf, opt.path, tuned.par, sel.var
#' The first list iterates the tasks, the second one the learners, both are named by respective IDs, the third list iterates the
#' resampling iterations. You can reduce these lists by using the optional arguments 'task' and 'learner'. 
#' 'drop' is by default TRUE, which means that the list structures are simplified as much as possible, if you don't want this set 'drop' to FALSE. 
#' 
#' \describe{
#'   \item{learners [character]}{IDs of learners used in experiment.}
#'   \item{tasks [character]}{IDs of tasks used in experiment.}
#'   \item{measures [character]}{Names of measures recorded in experiment.}
#' 	 \item{iters [numeric]}{Named numerical vector which lists the number of iterations for every task. Names are IDs of task.}
#' 	 \item{prediction [see above] }{List of list of predictions for every task/learner. }
#' 	 \item{conf.mat [see above] }{List of list of confusion matrices for every task/learner. }
#' 	 \item{opt.result [see above] }{List of list of list of \code{\linkS4class{opt.result}} for every task/learner/iteration. Entry is NULL if no optimization was done.}
#' 	 \item{opt.perf [see above] }{List of list of list of performance vectors of optimal settings for every task/learner/iteration. Note that this performance refers to the inner resampling! Entry is NULL if no optimization was done.}
#' 	 \item{opt.par [see above] }{List of list of list of optimal settings for every task/learner/iteration. Entry is NULL if no optimization was done.}
#' 	 \item{opt.path [see above] }{List of list of list of optimization paths for every task/learner/iteration. Entry is NULL if no optimization was done.}
#' 	 \item{tuned.par [see above] }{List of list of list of optimal hyperparameters for every task/learner/iteration. Entry is NULL if no tuning was done. Basically a different name for "opt.par".}
#' 	 \item{sel.var [see above] }{List of list of list of optimal features for every task/learner/iteration. Entry is NULL if no feature selection was done.. Basically a different name for "opt.par".}
#'   \item{perf [see above] }{List of 3 dim. arrays of performance values for every data set.}
#' }
#' 
#' @rdname bench.result-class
#' @exportClass bench.result
#' @title bench-result
#' @seealso \code{\link{bench.exp}}


setClass(
		"bench.result",
		contains = c("object"),
		representation = representation(
				task.descs = "list",
				data.descs = "list",
				resamplings = "list",
				perf = "list",
				predictions = "list",
				models = "list",
				conf.mats = "list",
				opt.results = "list" 
		)
)

#' @rdname bench.result-class

setMethod(
		f = "[",
		signature = signature("bench.result"),
		def = function(x,i,j,...,drop) {
			mylistdrop = function(y) {
				if(is.data.frame(y) || !is.list(y)) 
					y
				else {
					if (length(y) == 1)
						mylistdrop(y[[1]])
					else
						lapply(y, mylistdrop)
				}
			}
			
			mydrop = function(y) {
				if(!drop)
					return(y)
				z = mylistdrop(y)
				rec.lapply(z, function (w) {
					if(is.array(w)) drop(w) 
					else if(is.data.frame(w)) w[,,drop=TRUE]
					else w
				})
			}
			
			if (i == "iters") {
				return(sapply(x@perf, function(y) return(dim(y)[1] - 1)))
			}
			if (i == "learners") {
				return(dimnames(x@perf[[1]])[[2]])
			}
			if (i == "measures") {
				return(dimnames(x@perf[[1]])[[3]])
			}
			if (i == "tasks") {
				return(names(x@perf))
			}

			
			args = list(...)
			as.data.frame = args$as.data.frame
			
			task = args$task
			if (is.null(task))
				task = x["tasks"]
      rest.task = setdiff(task, x["tasks"])
      if (length(rest.task)>0)
        stop("Task ids are not in bench.result: ", paste(rest.task, collapse=", "))      
      learner = args$learner
			if (is.null(learner))
				learner = x["learners"]
      rest.learner = setdiff(learner, x["learners"])
      if (length(rest.learner)>0)
        stop("Learner ids are not in bench.result: ", paste(rest.learner, collapse=", "))      
			measure = args$measure
			if (is.null(measure))
				measure = x["measures"]
      rest.measure = setdiff(measure, x["measures"])
      if (length(rest.measure)>0)
        stop("Measures are not in bench.result: ", paste(rest.measure, collapse=", "))      
      iter = args$iter
			if (is.null(iter))
				iter = lapply(x["iters"][task], function(y) 1:y)
			aggr = args$aggr
			if (is.null(aggr))
				aggr=list()
      else if (any(sapply(aggr, function(a) identical(a, "resampling")))) {
        if (length(aggr) > 1) 
          stop("If you use aggr='resampling', you cannot pass other aggregation functions currently!")
        ress = x@resamplings
        aggr = lapply(ress, function(x) x["aggr.iter"])
        if(any(sapply(aggr, function(a) !identical(a, aggr[[1]]))))
          stop("If you use aggr='resampling', the aggregation functions of all resampling strategies in the bench.exp have to be identical currently!")
        aggr = aggr[[1]]        
      }
			aggr = make.aggrs(aggr)
			
			if (i == "prediction"){
				# reduce to selected tasks / learners
				preds = x@predictions[task]
				preds = lapply(preds, function(y) y[learner])
				return(mydrop(preds))
			}		
			# reduce to selected tasks
			ors = x@opt.results[task]
			# reduce to selected learners
			ors = lapply(ors, function(y) y[learner])
			if (i == "opt.result"){
				return(mydrop(ors))
			}
			if (i == "opt.par"){
				return(mydrop(rec.lapply(ors, function(y) y["par"])))
			}
			if (i == "tuned.par"){
				tps = rec.lapply(ors, function(y) y["tuned.par"])
				if (!is.null(as.data.frame) && as.data.frame) {
					tps = rec.lapply(tps, function(y) as.data.frame(y), depth=3)
					tps = rec.lapply(tps, function(y) Reduce(rbind, y), depth=2) 
				}
				return(mydrop(tps))
			}
			if (i == "sel.var"){
				return(mydrop(rec.lapply(ors, function(y) y["sel.var"])))
			}
			if (i == "opt.perf"){
				return(mydrop(rec.lapply(ors, function(y) y["perf"])))
			}
			if (i == "opt.path"){
				return(mydrop(rec.lapply(ors, function(y) y["path", as.data.frame=as.data.frame])))
			}
			if (i == "conf.mat"){
        # reduce to selected tasks / learners
        cms = x@conf.mats[task]
        cms = lapply(cms, function(y) y[learner])
				return(mydrop(cms))
			}
			
			if (i == "perf") {
				# reduce to selected tasks
				p = x@perf[task]
				# reduce to selected elements
				if (is.null(aggr$combine))
					g = function(arr, is) arr[is, learner, measure, drop=FALSE]
				else			
					g = function(arr, is) arr[c(is, "combine"), learner, measure, drop=FALSE]
				p = Map(g, p, is=iter)
				# aggregate
				if (length(aggr) > 0) {
					gg = function(arr) {
						lapply(names(aggr), function(nn) {
									if (nn == "combine")
										h = function(y) y[length(y)]
									else {
										# dont choose combine el from array
										last.el = ifelse(is.null(aggr$combine), 0, 1)
                    h = function(y) aggr[[nn]](y[1:(length(y)-last.el)])
									}
									t(apply(arr, c(2,3), h))
								})
					}	
					p = lapply(p, gg) 
					# put all aggr. values as columns together
					# combine aggr names with measure names
					aggr.ms = sapply(names(aggr), function(a) paste(a, measure, sep="."))
					p = lapply(p, function(arrs) {
								y = Reduce(rbind, arrs)
								rownames(y) = aggr.ms  
								return(y)
					})
				}
				return(mydrop(p))
			}
			callNextMethod()
		}
)






#' @rdname to.string

setMethod(
		f = "to.string",
		signature = signature("bench.result"),
		def = function(x) {
			p = x["perf", aggr=list(mean=mean, sd=sd)]
			p = paste(capture.output(p), collapse="\n")
		}
)


### todo: pretty print method for this case: only aggregated values, always the same learners



