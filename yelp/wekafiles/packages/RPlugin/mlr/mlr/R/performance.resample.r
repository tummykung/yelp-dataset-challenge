#' @include prediction.resample.r
roxygen()

#' @rdname performance

setMethod(
		f = "performance",
		signature = c(pred="resample.prediction", measures="list", losses="list", aggr="list"),
		def = function(pred, measures, aggr, losses, task) {
			rin = pred["instance"]
			preds = as.list(pred)
			is = 1:rin["iters"]
			perfs = lapply(preds, function(p) performance(p, measures=measures, aggr=rin["aggr.group"], losses=losses, task=task))
			# add iter index to measure data.frame
			ms.list = lapply(is, function (i) {
				m=perfs[[i]]$measures 
				if(is.numeric(m)) 
					m = as.matrix(t(m)) 
				cbind(iter=i, m)
			})
			# put together without aggregation
			ms.all = Reduce(rbind, ms.list)
			if (rin["has.groups"]) {
				ms.aggr.group = Reduce(rbind, lapply(perfs, function(x) x$aggr))
#				if (!is.matrix(ms.aggr.group))
#					ms.aggr.group = as.matrix(t(ms.aggr.group))
#				colnames(ms.aggr.group) = names(measures)
				rownames(ms.aggr.group) = NULL
				ms.aggr.group = as.data.frame(ms.aggr.group)
				ms.aggr = lapply(aggr, function(f) apply(ms.aggr.group, 2, f))
				ms.aggr.group = cbind(iter=is, ms.aggr.group)
			} else {
				ms.aggr = lapply(aggr, function(f) apply(ms.all[,-1,drop=FALSE], 2, f))
			}				
			j = which(names(aggr) == "combine")
			if (length(j) > 0) {
				# downcast 
				if (rin["has.groups"]) {
					pred = as(pred, "grouped.prediction")
          ms.aggr[[j]] = performance(pred=pred, measures=measures, losses=list(), aggr=rin["aggr.group"], task=task)$aggr
        } else {
					pred = as(pred, "prediction")
          ms.aggr[[j]] = performance(pred=pred, measures=measures, losses=list(), aggr=list(), task=task)$measures
        }
			}
			ms.aggr = Reduce(rbind, ms.aggr)
			if (!is.matrix(ms.aggr))
				ms.aggr = as.matrix(t(ms.aggr))
			colnames(ms.aggr) = names(measures)
			rownames(ms.all) = NULL 
			rownames(ms.aggr) = names(aggr)
			ms.all = as.data.frame(ms.all)
			ms.aggr = as.data.frame(ms.aggr)
			
			ls = lapply(is, function (i) cbind(iter=i, perfs[[i]]$losses))
			ls = as.data.frame(Reduce(rbind, ls))

			result = list(measures=ms.all)	
			if (rin["has.groups"])
				result$aggr.group = ms.aggr.group
			result$aggr = ms.aggr
			if (length(losses) > 0)
				result$losses = ls
			return(result)
		}
)

