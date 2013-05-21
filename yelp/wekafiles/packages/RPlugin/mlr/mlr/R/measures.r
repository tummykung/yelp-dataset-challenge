# A performance measure transforms a vector of predictions (compared to the true responses) into a single numerical value. 
#
#' Performance measures can always be passed as a single string (name of a single measure), a character vector of multiple names of measures or 
#' a list containing string names of measures and your on performance measures as function objects. The latter ones should 
#' be named list elements.\cr  
#' 
#' Classification: 
#' \itemize{ 
#' 		\item{\bold{mmce}}{\cr Mean misclassification error}
#' 		\item{\bold{acc}}{\cr Accuracy}
#' 		\item{\bold{costs}}{\cr Misclassification costs according to cost matrix}
#' 		\item{\bold{tp}}{\cr True positives}
#' 		\item{\bold{tpr, hit-rate, recall}}{\cr True positive rate}
#' 		\item{\bold{fp, false-alarm}}{\cr False positives}
#' 		\item{\bold{fpr, false-alarm-rate, fall-out}}{\cr False positive rate}
#' 		\item{\bold{tn, correct-rejection}}{\cr True negatives}
#' 		\item{\bold{tnr, specificity}}{\cr True negative rate}
#' 		\item{\bold{fn, miss}}{\cr False negatives}
#' 		\item{\bold{fnr}}{\cr False negative rate}
#' 		\item{\bold{ppv, precision}}{\cr Positive predictive value}
#' 		\item{\bold{npv}}{\cr Negative predictive value}
#' 		\item{\bold{fdr}}{\cr False discovery rate}
#' 		\item{\bold{f1}}{\cr F1 measure}
#' 		\item{\bold{mcc}}{\cr Matthews correlation coefficient}
#' 		\item{\bold{gmean}}{\cr G-mean, geomentric mean of recall and specificity.}
#' 		\item{\bold{gpr}}{\cr Geometric mean of precision and recall.}
#' 		\item{\bold{auc}}{\cr Area under the curve.}
#' 
#' 		\item{\bold{time.train}}{\cr Time of fitting the model}
#' 		\item{\bold{time.predict}}{\cr Time of predicting test set}
#' 		\item{\bold{time}}{\cr time.train + train.predict}
#' }
#' 
#' Regression:
#' \itemize{ 
#' 		\item{\bold{sse}}{\cr Sum of squared errors}
#' 		\item{\bold{mse}}{\cr Mean of squared errors}
#' 		\item{\bold{medse}}{\cr Median of squared errors}
#' 		\item{\bold{sae}}{\cr Sum of absolute errors}
#' 		\item{\bold{mae}}{\cr Mean of absolute errors}
#' 		\item{\bold{medae}}{\cr Median of absolute errors}
#' 
#' 		\item{\bold{time.train}}{\cr Time of fitting the model}
#' 		\item{\bold{time.predict}}{\cr Time of predicting test set}
#' 		\item{\bold{time}}{\cr time.train + train.predict}
#' }
#'  
#' @title Performance measures.
measures = function() {}




make.measures = function(xs) {
	if (length(xs)==0)
		return(list())
	ys = list()
	# single function to list
	if (is.function(xs)) {
		xs = list(xs)
	}
	for (i in 1:length(xs)) {
		x = xs[[i]] 
		if (is.function(x))
			y = x
		else if (is.character(x))
			y =make.measure(x)
		ys[[i]] = y
		nn = names(xs)[i]
		if (is.null(nn) || nn == "")
			nn = attr(y, "id")
		if (is.null(nn))
			stop("No name for measure! Set an attribute 'id' on the measure or pass measures as a named list!")
		names(ys)[i] = nn
	}
	return(ys)	
}


make.measure <- function(x) {
	name = x
	if (name == "acc") 
		x = acc
	else if (name == "mmce") 
		x = mce
	else if (name == "costs") 
		x = cost.measure
	
	else if (name=="tp") 
		x = tp
	else if (name %in% c("tpr", "hit-rate", "recall")) 
		x = tpr
	else if (name %in% c("fp", "false-alarm")) 
		x = fp
	else if (name %in% c("fpr", "false-alarm-rate", "fall-out")) 
		x = fpr
	else if (name %in% c("tn", "correct-rejection")) 
		x = tn
	else if (name %in% c("tnr", "specificity")) 
		x = tnr
	else if (name %in% c("fn", "miss")) 
		x = fn
	else if (name=="fnr") 
		x = fnr

	else if (name %in% c("ppv", "precision")) 
		x = ppv
	else if (name=="npv") 
		x = npv
	else if (name=="fdr") 
		x = fdr
	else if (name=="mcc") 
		x = mcc
	else if (name=="f1") 
		x = f1
	else if (name=="gmean") 
		x = gmean
	else if (name=="gpr") 
		x = gpr
	else if (name=="auc") 
		x = auc
	
	else if (name=="sse") 
		x = sse
	else if (name=="mse") 
		x = mse
	else if (name=="medse") 
		x = medse
	else if (name=="sae") 
		x = sae
	else if (name=="mae") 
		x = mae
	else if (name=="medae") 
		x = medae
	
	else if (name=="time") 
		x = time.all
	else if (name=="time.train") 
		x = time.train
	else if (name=="time.predict") 
		x = time.predict
	
	else 
		stop("Requested unknown measure: ", name)
	
	attr(x, "id") = name
	return(x)
}



default.measures = function(x) {
	if (x["is.classif"])
		return(make.measures("mmce"))
	else 
		return(make.measures("mse"))
}




### classification


acc = function(x, task) {
	mean(as.character(x["truth"]) == as.character(x["response"])) 
}
mce = function(x, task) {
	mean(as.character(x["truth"]) != as.character(x["response"])) 
}
sme = function(x, task) {
	sum(as.character(x["truth"]) != as.character(x["response"])) 
}

mcesd = function(x, task) {
	sd(as.character(x["truth"]) != as.character(x["response"])) 
}

cost.measure = function(x, task, costs=task["costs"]) {
	if (all(dim(costs) == 0))
		stop("No costs were defined in task!")
  # cannot index with NA
  if (any(is.na(x["response"])))
    return(as.numeric(NA))
	cc = function(truth, pred) {
		costs[truth, pred]
	}
	m = Reduce(sum, Map(cc, as.character(x["truth"]), as.character(x["response"])))
	return(m)
}

make.cost.measure = function(task, costs) {
	#todo checks
	force(costs)
	function(x, task) cost.measure(x, task, costs=costs)
}

### binary



tp = function(x, task) {
	sum(x["truth"] == x["response"] & x["response"] == x@task.desc["positive"])  
}
tn = function(x, task) {
	sum(x["truth"] == x["response"] & x["response"] == x@task.desc["negative"])  
}
fp = function(x, task) {
	sum(x["truth"] != x["response"] & x["response"] == x@task.desc["positive"])  
}
fn = function(x, task) {
	sum(x["truth"] != x["response"] & x["response"] == x@task.desc["negative"])  
}




tpr = function(x, task) {
	tp(x) / sum(x["truth"] == x@task.desc["positive"])  
}
fpr = function(x, task) {
	fp(x) / sum(x["truth"] == x@task.desc["negative"])  
}
tnr = function(x, task) {
	1 - fpr(x)  
}
fnr = function(x, task) {
	1 - tpr(x)  
}


ppv = function(x, task) {
	tp(x) / sum(x["response"] == x@task.desc["positive"])  
}
npv = function(x, task) {
	tn(x) / sum(x["response"] == x@task.desc["negative"])  
}
fdr = function(x, task) {
	fp(x) / sum(x["response"] == x@task.desc["positive"])  
}
mcc = function(x, task) {
	(tp(x) * tn(x) -
	fp(x) * fn(x)) /
	sqrt(prod(table(x["truth"], x["response"])))
}
f1 = function(x, task) {
	2 * tp(x) /
	(sum(x["truth"] == x@task.desc["positive"]) + sum(x["response"] == x@task.desc["positive"]))  
}
gmean = function(x, task) {
	sqrt(tpr(x)* tnr(x))
}

gpr = function(x, task) {
	sqrt(ppv(x) * tpr(x))
}

auc = function(x, task) {
	# ROCR does not work with NAs
  # if we have only 
  if (any(is.na(x["response"])) || length(unique(x["truth"])) == 1)
		return(as.numeric(NA))
	rpreds = as.ROCR.preds(x)
	ROCR.performance(rpreds, "auc")@y.values[[1]]
}



### regression


sse = function(x, task) {
	sum((x["truth"] - x["response"])^2) 
}

mse = function(x, task) {
	mean((x["truth"] - x["response"])^2) 
}

medse = function(x, task) {
	median((x["truth"] - x["response"])^2) 
}


sae = function(x, task) {
	sum(abs(x["truth"] - x["response"])) 
}

mae = function(x, task) {
	mean(abs(x["truth"] - x["response"])) 
}

medae = function(x, task) {
	median(abs(x["truth"] - x["response"])) 
}


### time

time.all = function(x, task) {
	time.train(x) + time.predict(x)  
}
time.train = function(x, task) {
	if (is(x, "resample.prediction"))
		NA
	else
		x["time.train"]  
}
time.predict = function(x, task) {
	if (is(x, "resample.prediction"))
		NA
	else
		x["time.predict"]  
}

