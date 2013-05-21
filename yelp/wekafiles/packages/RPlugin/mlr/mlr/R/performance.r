#' @include prediction.r
roxygen()

#' Measures the quality of predictions w.r.t. some performance measures or loss functions.
#' 
#' @param pred [\code{\linkS4class{prediction}}] \cr
#' 		  Prediction object to evaluate.
#' @param measures [see \code{\link{measures}}]
#'        Performance measures. 
#' @param aggr [see \code{\link{aggregations}}]
#'        Aggregation functions. 
#' 		  Ignored if not a \code{\linkS4class{resample.prediction}}
#' @param losses [see \code{\link{losses}}]
#'        Loss functions. 
#' @param task [\code{\linkS4class{learn.task}}]\cr 
#'        Optionally specifies learning task, very rarely needed.
#' 
#' @return A list with with possibly three named components: "measures" is a data.frame of performance values,
#' 		   "aggr" a data.frame of aggregated values, "losses" a data.frame of losses. 
#' 
#' @exportMethod performance
#' @rdname performance
#' 
#' @usage performance(pred, measures, aggr, losses, task)
#'
#' @title Measure performance and losses of prediction.



setGeneric(
		name = "performance",
		def = function(pred, measures, aggr, losses, task) {
			if (missing(measures))
				measures=default.measures(pred@task.desc)
			measures = make.measures(measures)
			if (missing(losses))
				losses=list()
			losses = make.losses(losses)
			if(missing(aggr))
				aggr = default.aggr(pred)
			aggr = make.aggrs(aggr)
			standardGeneric("performance")
		}
)

#' @rdname performance

setMethod(
		f = "performance",
		signature = signature(pred="prediction", measures="list", aggr="list", losses="list"),
		def = function(pred, measures, aggr, losses, task) {
			x = pred
			td = x@task.desc
			dd = x@data.desc			
			ms = sapply(measures, function(f) f(x, task=task))	
			names(ms) = names(measures)
			
			if (length(losses) > 0) {
				ls = lapply(losses, function(f) f(x, task=task))
				ls = as.data.frame(Reduce(cbind, ls))
				colnames(ls) = names(losses)
				if (!is.null(x["id"]))
					ls = cbind(id=x["id"], ls)
			}
			
			if (length(losses) > 0)
				return(list(measures=ms, losses=ls))
			return(list(measures=ms))
		}
)



