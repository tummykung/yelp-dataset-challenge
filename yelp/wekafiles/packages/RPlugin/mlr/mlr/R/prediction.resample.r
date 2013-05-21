#' @include task.learn.r
#' @include resample.instance.r
roxygen()

#' @exportClass resample.prediction

setClass(
		"resample.prediction",
		contains = c("prediction"),
		representation = representation(
				instance="resample.instance", 
				extracted="list"
		)
)

#' Constructor.

setMethod(
		f = "initialize",
		signature = signature("resample.prediction"),
		def = function(.Object, instance, preds, extracted) {
			p1 = preds[[1]]
			.Object@instance = instance
			.Object@extracted = extracted
			type = p1["type"]
			df = Reduce(function(a,b) rbind(a, b@df), preds, init=data.frame())
			threshold = p1["threshold"]
			tp = sapply(preds, function(x) x["time.predict"])
			tt = sapply(preds, function(x) x["time.train"])
			es = sapply(preds, function(x) nrow(x@df))
			df$iter = rep(1:length(preds), times=es)

			.Object@type = type			
			.Object@df = df			
			.Object@threshold = threshold			
			.Object@data.desc = p1@data.desc			
			.Object@task.desc = p1@task.desc	
			.Object@time.train = tt			
			.Object@time.predict = tp
			return(.Object)
		}
)




#' @rdname to.string

setMethod(
		f = "to.string",
		signature = signature("resample.prediction"),
		def = function(x) {
			return(
					paste(
							"Resampling result for: ", to.string(x@instance@desc),
							#"Learner models were ", ifelse(length(x@models)==0,"not", ""), " saved\n\n",
							#paste(capture.output(str(x@preds)), collapse="\n"), 
							"\n", sep=""
					)
			)
		}
)

#' Getter
#'
#' Note that in the case of the "prob", "response" and "decision"
#' fields, the results are returned in the order they were used by
#' the resampling strategy and not in the order present in the
#' dataset. This mainly applies to cross-validation were a different
#' order might be expected.
#'
#' @rdname resample.prediction-class
setMethod(
		f = "[",
		signature = signature("resample.prediction"),
		def = function(x,i,j,...,drop) {
			if (i == "iters")
				return(x@instance["iters"])
			callNextMethod()
		}
)

#' Converts object to a list of normal prediction objects - one for each iteration.
#' @rdname resample.prediction-class

setMethod(
		f = "as.list",
		signature = signature("resample.prediction"),
		def = function(x, all.names = FALSE, ...) {
			df = x@df
			iter = as.factor(df$iter)
			df = subset(df, select=-iter)
			dfs = split(df, iter) 
			preds = list()
      cl = ifelse(x["has.groups"], "grouped.prediction", "prediction")		
			for(i in 1:x@instance["iters"]) {
				y = dfs[[i]]
				preds[[i]] = new(cl, task.desc=x@task.desc, data.desc=x@data.desc, 
						type=x@type, df=y, threshold=x@threshold, x@time.train[i], x@time.predict[i])						
				
			}
			return(preds)
		}
)


setAs("resample.prediction", "prediction", 
		function(from, to) {
			df = from@df
			df$iter = NULL
			df$group = NULL
			new("prediction", task.desc=from@task.desc, data.desc=from@data.desc, 
					type=from@type, df=df, threshold=from@threshold, sum(from@time.train), sum(from@time.predict))						
		}
)

setAs("resample.prediction", "grouped.prediction", 
		function(from, to) {
			df = from@df
			df$iter = NULL
			new("grouped.prediction", task.desc=from@task.desc, data.desc=from@data.desc, 
					type=from@type, df=df, threshold=from@threshold, sum(from@time.train), sum(from@time.predict))
		}
)
