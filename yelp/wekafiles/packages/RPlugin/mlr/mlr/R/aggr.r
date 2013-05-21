#' Aggregation functions. You can use any function in R which reduces a numerical vector to simple real number. 
#' Refer to the function either by using an R function object or by using a string denoting the built-in function.
#' If you choose to pass an R function, you should set the attribute 'id' to a string to give the function a name.  
#' You can use multiple aggregation functions if you pass a list or vector of the former.  
#' @title Aggregation functions.
aggregations = function() {}

make.aggrs = function(xs) {
	if (length(xs)==0)
		return(list())
	if (is.function(xs)) {
		nn = deparse(substitute(xs))
		xs = list(xs)
		names(xs) = nn
	}
	ys = list()
	for (i in 1:length(xs)) {
		x = xs[[i]] 
		if (is.function(x)) {
			y = x
		}
		else if (is.character(x)) {
			if (x == "combine") {
				y = function(...) NA
			}
			else {	
				y = get(x)
				if (!is.function(y)) {
					stop("Aggregation method is not the name of a function: ", x)
				}
			}
			attr(y, "id") = x
		}
		ys[[i]] = y
		nn = names(xs)[i]
		if (is.null(nn))
			nn = attr(y, "id")
		if (is.null(nn))
			stop("No name for aggregation method: ", capture.output(str(y)))
		names(ys)[i] = nn
	}
	return(ys)	
}

default.aggr = function(x) {
	if (is(x, "resample.desc"))
		x["aggr.iter"]
	else if (is(x, "resample.instance"))
		default.aggr(x@desc)
	else if (is(x, "resample.prediction"))
		default.aggr(x@instance)
	else
		return(list("mean", "sd"))
}

