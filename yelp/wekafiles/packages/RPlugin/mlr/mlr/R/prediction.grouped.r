
setClass(
		"grouped.prediction",
		contains = c("prediction")
)

#' Constructor.
setMethod(
		f = "initialize",
		signature = signature("grouped.prediction"),
		def = function(.Object, data.desc, task.desc, type, df, threshold, time.train, time.predict) {
			.Object = callNextMethod(.Object, data.desc, task.desc, type, df, threshold, time.train, time.predict)
			return(.Object)
		}
)

#' Converts object to a list of lists of normal prediction objects - one for each iteration and group
#' @rdname resample.prediction-class

setMethod(
		f = "as.list",
		signature = signature("grouped.prediction"),
		def = function(x, all.names = FALSE, ...) {
			df = x@df
			group = df$group
			df = subset(df, select=-group)
			dfs = split(df, group) 
			preds = lapply(dfs, function(y) {
						new("prediction", task.desc=x@task.desc, data.desc=x@data.desc, 
								type=x@type, df=y, threshold=x@threshold, x@time.train, x@time.predict)						
			})
			return(preds)
		}
)



