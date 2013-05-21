setClass(
		"novars",
		contains = c("object"),
		representation = representation(
				task.desc = "task.desc",
				data.desc = "data.desc",
				targets = "vector"
		)
)


predict_novars = function(object, newdata, type) {
	m = object
	tars = m["targets"]
	# for regression return constant mean
	if (m["task.desc"]["is.regr"])
		return(rep(mean(tars), nrow(newdata)))
	tab = prop.table(table(tars))
	probs = as.numeric(tab) 
	if(type=="response")
		return(sample(as.factor(names(tab)), nrow(newdata), prob=probs, replace=TRUE))	
	else {
		probs = t(replicate(nrow(newdata), probs))
		colnames(probs) = names(tab)
		return(probs)
	}
}
