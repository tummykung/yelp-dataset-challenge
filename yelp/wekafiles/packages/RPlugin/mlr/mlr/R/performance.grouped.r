#' @include prediction.grouped.r
roxygen()
#' @include performance.r
roxygen()

#' @rdname performance

setMethod(
		f = "performance",
		signature = c(pred="grouped.prediction", measures="list", losses="list", aggr="list"),
		def = function(pred, measures, aggr, losses, task) {
			preds = as.list(pred)
			grp.perf = lapply(preds, function(p) performance(p, measures=measures, losses=losses, task=task))
			gp = lapply(grp.perf, function(x) x$measures)
			ms = as.data.frame(Reduce(rbind, gp))
			rownames(ms) = NULL
			# we only have one aggregate function for groups
			aggr = aggr[[1]]
			group.obs = pred["group"]
			group.names = names(preds)
			ms2 = aggr(ms, group.names, pred=pred)
			ms = cbind(group=group.names, ms)
			ls = callNextMethod(pred=pred, measures=list(), losses=losses, aggr=list(), task=task)$losses
			if (length(losses) > 0) {
				ls = cbind(ls[, 1, drop=FALSE], group=group.obs, ls[, -1, drop=FALSE])  
				return(list(measures=ms, aggr=ms2, losses=ls))
			} else
				return(list(measures=ms, aggr=ms2))
	}
)

