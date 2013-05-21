#' @include benchexp.r
roxygen()

#' Compare ROC curves of learners in a benchmark experiment.
#' 
#' @param x [\code{\linkS4class{bench.result}}] \cr
#'   Benchmark result. Must contain predictions.
#' @param task.id [string] \cr
#'   Id of a task from \code{x}. 
#' @param learner.ids [character] \cr
#'   Ids of learners to plot. Default are all learners in \code{x} for selected task. 
#' @param perf1 [string] \cr
#'   First ROC measure for y-axis. Note that this must be a ROCR measure, see \code{\link{ROCR.performance}}. 
#'   Default is "tpr".
#' @param perf2 [string] \cr
#'   Second ROC measure for x-axis. Note that this must be a ROCR measure, see \code{\link{ROCR.performance}}. 
#'   Default is "fpr".
#' @param legend.x [any] \cr 
#'   Where should legend be placed. Any placement accepted by \code{\link{legend}} is ok.
#'   "none" does not plot a legend. 
#' @param legend.y [any] \cr 
#'   Where should legend be placed. Any placement accepted by \code{\link{legend}} is ok.
#' @param col [character] \cr 
#'   Colors for ROC curves. Default are rainbow colors.
#' @param ... [any] \cr 
#'   Further arguments that are passed to \code{\link{ROCR.plot.performance}}.
#' 
#' @export
#' @rdname ROCR.plot.task
#' @seealso \code{\link{ROCR.plot.performance}}
#' 
#' @title Compare ROC curves of learners in a benchmark experiment.


ROCR.plot.task = function(x, task.id, learner.ids=x["learners"], 
  perf1="tpr", perf2="fpr", 
  legend.x="bottomright", legend.y, col, ...) {
  
  n = length(learner.ids)
  
  if (missing(task.id)) {
    task.id = x["tasks"]
    if (length(task.id) > 1)
      stop("bench.result contains more than 1 task, please pass task.id!")
  }
  preds = x["prediction", learner=learner.ids, task=task.id, drop=F][[1]]

  if (length(preds) != n)
    stop("bench.result must contain predictions for all selected learners!")

  if (missing(col))
    col = rainbow(n)
  # recycle if not enuff
  col = rep(col, length=n)
  
  for (i in 1:n) {
    id = learner.ids[i]
    p = preds[[id]]
    if(!any(is.na(p["response"]))) {
      p = as.ROCR.preds(p)
      perf = ROCR.performance(p, perf1, perf2)
      add = (i != 1)
        plot(perf, add=add, col=col[i], ...)
    }
  }
  if(legend.x != "none")
    legend(legend.x, legend.y, legend=learner.ids, col=col, fill=col)
}
