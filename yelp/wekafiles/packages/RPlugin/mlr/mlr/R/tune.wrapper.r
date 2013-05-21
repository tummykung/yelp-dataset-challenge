
#' Fuses a base learner with a search strategy to select its hyperparameters. Creates a learner object, which can be
#' used like any other learner object, but which internally uses tune. If the train function is called on it, the search strategy and resampling are invoked
#' to select an optimal set of hyperparameter values. Finally, a model is fitted on the complete training data with these optimal
#' hyperparameters and returned.    
#'
#' @param learner [\code{\linkS4class{learner}} or string]\cr 
#'        Learning algorithm. See \code{\link{learners}}.  
#' @param id [string] \cr
#'        Id for resulting learner object. If missing, id of "learner" argument is used.
#' @param label [string] \cr
#'        Label for resulting learner object. If missing, label of "learner" argument is used.
#' @param resampling [\code{\linkS4class{resample.instance}}] or [\code{\linkS4class{resample.desc}}]\cr
#'        Resampling strategy to evaluate points in hyperparameter space.
#' @param control 
#'        Control object for search method. Also selects the optimization algorithm for tuning.   
#' @param measures [see \code{\link{measures}}]
#'        Performance measures. 
#' @param aggr [see \code{\link{aggregations}}]
#'        Aggregation functions. 
#' 
#' @return \code{\linkS4class{learner}}.
#' 
#' @export
#'
#' @seealso \code{\link{tune}}, \code{\link{grid.control}}, \code{\link{optim.control}}, \code{\link{cmaes.control}} 
#'   
#' @title Fuse learner with tuning.

make.tune.wrapper <- function(learner, id=as.character(NA), label=as.character(NA), resampling, control, measures, aggr) {
	make.opt.wrapper(learner, id, label, resampling, control, measures, aggr)
}

