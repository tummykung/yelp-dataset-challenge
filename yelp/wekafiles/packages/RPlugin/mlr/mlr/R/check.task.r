check.task <- function(data, target) {
	cns = colnames(data)
	x = duplicated(cns)
	if(any(x))
		stop("Duplicated column names in data.frame are not allowed: ", paste(cns[x], collapse=","))
	if (!(target %in% cns)) {
		stop(paste("Column names of data.frame don't contain target var: ", target))
	}
	
	# todo: rpart does not like (), bug there?
	forbidden  = c("[", "]", "(", ")", ",", " ")
	forbidden2 = c("[", "]", "(", ")", ",", "<WHITESPACE>")
	#forbidden = c("[", "]")
	i = sapply(forbidden, function(x) length(grep(x, cns, fixed=TRUE)) > 0)
	if (any(i))
		stop(paste("Column names should not contain: ", paste(forbidden2, collapse=" ")))
	if (any(is.na(data[, target]))) {
		stop("Target values contain missings!")
	}
	if (any(is.infinite(data[, target]))) {
		stop("Target values contain infinite values!")
	}
}
