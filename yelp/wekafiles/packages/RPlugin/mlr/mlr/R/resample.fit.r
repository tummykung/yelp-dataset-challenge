#' @include resample.instance.r
roxygen()
#' @include resample.instance.make.r
roxygen()
#' @include resample.desc.r
roxygen()
#' @include resample.desc.make.r
roxygen()
#' @include prediction.resample.r
roxygen()


#' Given a resampling strategy, which defines sets of training and test indices, 
#' \code{resample.fit} fits the selected learner using the training sets and performs predictions for the test sets. 
#' For construction of the resampling strategies use the factory methods \code{\link{make.res.desc}} and 
#' \code{\link{make.res.instance}}.
#' 
#' Optionally information from the fitted models, e.g. the complete model, can be extracted and returned.
#'
#' @param learner [\code{\linkS4class{learner}} or \code{\link{character}}]\cr 
#'        Learning algorithm.   
#' @param task [\code{\linkS4class{learn.task}}] \cr
#'        Learning task.
#' @param resampling [\code{\linkS4class{resample.desc}} or \code{\linkS4class{resample.instance}}] \cr
#'        Resampling strategy. 
#' @param par.vals [list] \cr 
#'        Named list of hyperparameter values. Will overwrite the ones specified in the learner object. Default is empty list.
#' @param vars [\code{\link{character}}] \cr 
#'        Vector of variable names to use in training the model. Default is to use all variables.
#' @param extract [\code{\link{function}}] \cr 
#' 		  Function used to extract information from fitted models, e.g. can be used to save the complete list of fitted models. 
#'        Default is to extract nothing. 
#' @return \code{\linkS4class{resample.prediction}}.
#' 
#' @export
#' @rdname resample.fit 
#' 
#' @usage resample.fit(learner, task, resampling, par.vals, vars, extract)
#'
#' @title Fit models according to a resampling strategy.


setGeneric(
		name = "resample.fit",
		def = function(learner, task, resampling, par.vals, vars, extract) {
			if (is.character(learner))
				learner = make.learner(learner)
			if (is(resampling, "resample.desc")) 
				resampling = make.res.instance(resampling, task=task)
			if (missing(par.vals))
				par.vals = list()
			if (missing(vars))
				vars <- task["input.names"]
			if (length(vars) == 0)
				vars <- character(0)			
			if (missing(extract))
				extract <- function(x){}
			standardGeneric("resample.fit")
		}
)

#' @export
#' @rdname resample.fit 
setMethod(
		f = "resample.fit",
		signature = signature(learner="learner", task="learn.task", resampling="resample.instance", 
				par.vals="list", vars="character", extract="function"),
		def = function(learner, task, resampling, par.vals, vars, extract) {
			n = task["size"]
			r = resampling["size"]
			if (n != r)
				stop(paste("Size of data set:", n, "and resampling instance:", r, "differ!"))
			
			rin = resampling
			iters = rin["iters"]
			
			if (is(rin, "resample.instance.nonseq")) {
				rs = mylapply(1:iters, resample.fit.iter, from="resample", learner=learner, task=task, 
						rin=rin, par.vals=par.vals, vars=vars, extract=extract)
			} else {
				rs  = list()
				# sequential resampling cannot be (easily) parallized!
				i = 1
				while (!resample.done(rin)) {
					train.i = get.train.set(rin, i)
					ts = get.test.set(rin, i)
					test.i = ts$inds
					g = ts$group
					m = train(learner, task, subset=train.i, par.vals=par.vals, vars=vars)
					p = predict(m, task=task, subset=test.i, group=g)
					ex = extract(m)
					rs[[i]] = list(pred=p, extracted=ex)
					rin = resample.update(rin, task, m, p)
					i = i + 1
				}				
			}
		
			ps = lapply(rs, function(x) x$pred)
			es = lapply(rs, function(x) x$extracted)
			
			return(new("resample.prediction", instance=rin, preds=ps, extracted=es))
		}
)

