#' include wrapped.model.r
roxygen()

#' Predict the target variable of new data using a fitted model. If the type is set to "prob" or "decision"
#' probabilities or decision values will be stored in the resulting object. The resulting class labels are 
#' the classes with the maximum values or thresholding can also be used.
#' 
#' @param object [\code{\linkS4class{wrapped.model}}] \cr 
#'        Wrapped model, trained from a learn task.  
#' @param task [\code{\linkS4class{learn.task}}]\cr 
#'        Specifies learning task. If this is passed, data from this task is predicted.   
#' @param subset [integer] \cr 
#'        Index vector to subset the data in the task to use for prediction. 
#' @param newdata [\code{\link{data.frame}}] \cr 
#'        New observations which should be predicted. Alternatively pass this instead of task. 
#' @param type [string] \cr
#'        Classification: "response" | "prob" | "decision", specifying the type to predict.
#'        Default is "response". "decision" is experimental.
#' 		  Ignored for regression.	 
#' @param threshold [numeric] \cr
#'        Threshold to produce class labels if type is not "response". 
#' 	      Currently only supported for binary classification and type="prob", where it represents the required predicted probability
#'        for the positive class, so that a positive class is predicted as "response".
#'        Default is 0.5 for type="prob".
#' 		  Ignored for regression.	 
#' @param group [factor] \cr
#'        Only for internal use! 
#'        Default is NULL.
#' @return \code{\linkS4class{prediction}}.
#'
#' @export
#' @rdname predict
#' @importFrom stats predict
#' @seealso \code{\link{train}}
#' @title Predict new data.


#todo decision
setMethod(
		f = "predict",
		signature = signature(object="wrapped.model"),
		def = function(object, task, newdata, subset, type, threshold, group=NULL) {
			if (!missing(task) && !missing(newdata)) 
				stop("Pass either a task object or a newdata data.frame to predict, but not both!")

			model = object
			wl = model["learner"]
			td = model@task.desc
			dd = model@data.desc
			
			if (missing(newdata)) {
				if (missing(subset))
					subset = 1:task["size"]
				newdata = task["data", row=subset]
			} else {
        if (!is.data.frame(newdata) || nrow(newdata) == 0)
          stop("newdata must be a data.frame with at least one row!")
				newdata = prep.data(dd["is.classif"], newdata, dd["target"], dd["excluded"], dd["prepare.control"])			
			}
			
			if (missing(type))
				type = wl["predict.type"]
			if (missing(threshold))
				threshold = wl["predict.threshold"]
			if (is.null(threshold))
				threshold = switch(type, response=numeric(0), prob=0.5, decision=0)

      # load pack. if we saved a model and loaded it later just for prediction this is necessary
      require.packs(wl["pack"], paste("learner", learner["id"]))
			
			cns = colnames(newdata)
			tn = dd["target"]
			t.col = which(cns == tn)
			# get truth and drop target col, if target in newdata
			if (length(t.col) == 1) {
				truth = newdata[, t.col]
				newdata = newdata[, -t.col, drop=FALSE]					
				
			} else {
				truth = NULL
			}
			
			# we can check this for regression as well as those return prob = FALSE
			if ("prob" == type && !wl["probs"]) {
				stop("Trying to predict probs, but ", wl["id"], " does not support that!")
			}
			if ("decision" == type && !wl["decision"]) {
				stop("Trying to predict decision values, but ", wl["id"], " does not support that!")
			}

			hps = wl["pars.setting"][wl["pars.predict"]]
			
			logger.debug(level="predict", "mlr predict:", wl["id"], "with pars:")
			logger.debug(level="predict", hps)
			logger.debug(level="predict", "on", nrow(newdata), "examples:")
			logger.debug(level="predict", rownames(newdata))
			
			if (wl["is.classif"]) {
				levs = dd["class.levels"]
			}
			
			response = NULL
			prob = decision = NULL
			time.predict = as.numeric(NA)
			
			# was there an error in building the model? --> return NAs
			if(is(model["learner.model"], "learner.failure")) {
				p = predict_nas(wl, model, newdata, type, levs, dd, td)
				time.predict = as.numeric(NA)
			} else {
				pars <- list(
						.learner = wl,
						.model = model, 
						.newdata=newdata
				)
				pars = c(pars, hps) 
				if (wl["is.classif"]) {
					pars$.type = type
				}
				#pars = insert.matching(pars, list()) 
				
				if(!is.null(.mlr.local$debug.seed)) {
					set.seed(.mlr.local$debug.seed)
					warning("DEBUG SEED USED! REALLY SURE YOU WANT THIS?")
				}
				
				if(is(model["learner.model"], "novars")) {
					p = predict_novars(model["learner.model"], newdata, type)
					time.predict = 0
				} else {
					if (.mlr.local$errorhandler.setup$on.learner.error == "stop")
						st = system.time(p <- do.call(pred.learner, pars), gcFirst=FALSE)
					else
						st = system.time(p <- try(do.call(pred.learner, pars), silent=TRUE), gcFirst=FALSE)
					time.predict = st[3]
					# was there an error during prediction?
					if(is(p, "try-error")) {
						msg = as.character(p)
						if (.mlr.local$errorhandler.setup$on.learner.error == "warn")
							warning("Could not predict the learner: ", msg)
						p = predict_nas(wl, model, newdata, type, levs, dd, td)
						time.predict = as.numeric(NA)
					}
				}
				if (wl["is.classif"]) {
					if (type == "response") {
						# the levels of the predicted classes might not be complete....
						# be sure to add the levels at the end, otherwise data gets changed!!!
						if (!is.factor(p))
							stop("pred.learner for ", class(wl), " has returned a class ", class(p), " instead of a factor!")
						levs2 = levels(p)
						if (length(levs2) != length(levs) || any(levs != levs2))
							p = factor(p, levels=levs)
						
					} else if (type == "prob") {
						if (!is.matrix(p))
							stop("pred.learner for ", class(wl), " has returned a class ", class(p), " instead of a matrix!")
						cns = colnames(p)
						if (is.null(cns) || length(cns) == 0)
							stop("pred.learner for ", class(wl), " has returned not the class levels as column names, but no column names at all!")
						if (!setequal(cns, levs))
							stop("pred.learner for ", class(wl), " has returned not the class levels as column names:", colnames(p))
					} else if (type == "decision") {
						if (!is.matrix(p))
							stop("pred.learner for ", class(wl), " has returned a class ", class(p), " instead of a matrix!")
					} else {
						stop(paste("Unknown type", type, "in predict!"))
					}	
				} else if (is(model, "wrapped.model.regr")) {
					if (class(p) != "numeric")
						stop("pred.learner for ", class(wl), " has returned a class ", class(p), " instead of a numeric!")
				}
				logger.debug(level="predict", "prediction:")
				logger.debug(level="predict", p)
			}
			if (missing(task))
				ids = NULL			
			else
				ids = subset
			make.prediction(data.desc=dd, task.desc=td, id=ids, truth=truth, 
					type=type, y=p, group=group, threshold=threshold,  
					time.train=model["time"], time.predict=time.predict)
		}
)

predict_nas = function(learner, model, newdata, type, levs, data.desc, task.desc) {
	if (learner["is.classif"]) {
		p = switch(type, 
				response = factor(rep(NA, nrow(newdata)), levels=levs),
				matrix(as.numeric(NA), nrow=nrow(newdata), ncol=length(levs), dimnames=list(NULL, levs))
		)
	} else {
		p = as.numeric(rep(NA, nrow(newdata)))
	}
	return(p)
}


