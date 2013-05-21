#' @include prediction.r
roxygen()


#' Converts predictions to a format package ROCR can handle.
#' 
#' @param x [character] \cr
#'		Predictions. 			
#' 
#' @exportMethod as.ROCR.preds 
#' @rdname as.ROCR.preds 
#' @title Convert to ROCR format.


setGeneric(
		name = "as.ROCR.preds",
		def = function(x) {
#			if(!require(ROCR)) {
#				stop(paste("Package ROCR is missing!"))
#			}
			standardGeneric("as.ROCR.preds")
		}
)

#' @export
#' @rdname as.ROCR.preds 
setMethod(
		f = "as.ROCR.preds",
		signature = signature(x="prediction"), 
		def = function(x) {
			if(x@data.desc["class.nr"] != 2) {
				stop("More than 2 classes!")
			}
			p = x["prob"] 
			if(is.null(p)) {
				stop("No probabilities in prediction object!")
			}
			ROCR.prediction(p, x["truth"], label.ordering=c(x@task.desc["negative"], x@task.desc["positive"]))
		}
)


#' @export
#' @rdname as.ROCR.preds 
setMethod(
  f = "as.ROCR.preds",
  signature = signature(x="resample.prediction"), 
  def = function(x) {
    if(x@data.desc["class.nr"] != 2) {
      stop("More than 2 classes!")
    }
    if(is.null(x["prob"])) {
      stop("No probabilities in prediction object!")
    }
    prob = x["prob"]
    iter = as.factor(x["iter"])
    prob = split(prob, iter)
    truth = split(x["truth"], iter)
    ROCR.prediction(prob, truth, label.ordering=c(x@task.desc["negative"], x@task.desc["positive"]))
  }
)



