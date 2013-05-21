#' Optimizes the threshold of prediction based on probabilities or decision values.
#' Currently only implemented for probabilities and binary classification. 
#' 
#' 
#' @param pred [\code{\linkS4class{prediction}}] \cr
#' 		  Prediction object to use for tuning the treshold.
#' @param measures [see \code{\link{measures}}]
#'        Performance measures.
#' @param aggr [see \code{\link{aggregations}}]
#'        Aggregation functions. 
#' 		  Ignored if not a \code{\linkS4class{resample.prediction}}
#' @param task [\code{\linkS4class{learn.task}}] \cr
#'        Learning task. Rarely neeeded, only when required for the performance measure. 
#' @param minimize [logical] \cr 
#'       Minimize performance measure? Default is TRUE.
#' @param thresholds [integer] \cr
#' 		  Number of thresholds to try in tuning.  	
#' 
#' @return A list with with the following components: "th" is the optimal threshold, pred a prediction object based on "th", 
#' 		  		"th.seq" a numerical vector of threhold values which were tried and "perf" their respective performance values.  	 
#'
#' @export
#' @seealso \code{\link{tune}}
#' @title Tune prediction threshold.

tune.threshold = function(pred, measures, aggr, task, minimize=TRUE, thresholds=10) {
	if (missing(measures))
		measures = default.measures(pred@task.desc)
	measures = make.measures(measures)
	if (missing(aggr))
		aggr = default.aggr(pred)
	aggr = make.aggrs(aggr)
  
  pos = pred@task.desc["positive"]
  neg = pred@task.desc["negative"]
  levs = pred@data.desc["class.levels"]
  probs = pred["prob"]
  
  # brutally return NA if we find any NA in the pred. probs...
  if (any(is.na(probs))) {
    return(list(th=NA, pred=pred, th.seq=numeric(0), perf=numeric(0)))
  }
  
	if (is.null(probs))
		stop("No probs in prediction! Maybe you forgot type='prob'?")
	f = function(x) {
		labels = prob.threshold(probs=probs, pos=pos, neg=neg, levels=levs, threshold=x)
		pred@df$response = labels
		perf = performance(pred, measures=measures, aggr=aggr, task=task)
		return(perf$aggr[1,1])
	}
	probs.sorted = sort(unique(probs))
	len = min(thresholds, length(probs.sorted))
	probs.seq = probs.sorted[seq(1, length(probs.sorted), length=len)]
	vals = sapply(probs.seq, f)
	if (minimize)
		j = which.min(vals)
	else
		j = which.max(vals)
	th = probs.seq[j]
	labels = prob.threshold(probs=probs, pos=pos, neg=neg, levels=levs, threshold=th)
	pred@df$response = labels
	return(list(th=th, pred=pred, th.seq=probs.seq, perf=vals))
}
