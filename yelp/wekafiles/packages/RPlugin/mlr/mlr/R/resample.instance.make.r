
#' Generates an instance object for a resampling strategy. 
#' 
#' @param x [string or  \code{\linkS4class{resample.desc}}] \cr
#' 	  "cv" for cross-validation, "stratcv" for stratified cross-validation,  "repcv" for repeated cross-validation,\cr
#'		"bs" for out-of-bag bootstrap, "bs632" for B632 bootstrap, "bs632plus" for B632+ bootstrap,\cr
#'    "subsample" for subsampling, "holdout" for holdout.	 			
#' @param task [\code{\link{integer}}] \cr
#'		Data of task to resample from. Prefer to pass this instead of \code{size}.
#' @param size [\code{\link{integer}}] \cr
#'		Size of the data set to resample.
#' @param iters [integer] \cr
#'		Number of resampling iterations. Not needed for "holdout". 	 			
#' @param ... [any] \cr
#'		Further parameters for strategies.\cr 
#'			split: Percentage of training cases for "holdout", "subsample".\cr
#'			reps: Repeats for "repcv"
#' 
#' @return A \code{\linkS4class{resample.instance}} object.
#' @export 
#' @rdname make.res.instance
#' @title Construct resampling instance

setGeneric(
		name = "make.res.instance",
		def = function(x, task, size, iters, ...) {
      if (!missing(size) && is.numeric(size))
        size = as.integer(size)
      if (!missing(iters) && is.numeric(iters))
        iters = as.integer(iters)
      if (identical(x, "holdout") && missing(iters))
        iters = as.integer(NA)
      standardGeneric("make.res.instance")
		}
)

#' @export 
#' @rdname make.res.instance


setMethod(
		f = "make.res.instance",
		signature = c(x="character", task="missing", size="integer", iters="integer"),
		def = function(x, task, size, iters, ...) {
			desc = make.res.desc(x, iters=iters, ...)
			cc = paste(x, "instance", sep=".")
			make.res.i(cc, desc=desc, size=size, task=NULL)
		}
)

#' @export 
#' @rdname make.res.instance

setMethod(
		f = "make.res.instance",
		signature = c(x="character", task="learn.task", size="missing", iters="integer"),
		def = function(x, task, size, iters, ...) {
			desc = make.res.desc(x, iters=iters, ...)
			cc = paste(x, "instance", sep=".")
			make.res.i(cc, desc=desc, task=task, blocking=task["blocking"])
		}
)


#' @export 
#' @rdname make.res.instance

setMethod(
		f = "make.res.instance",
		signature = c(x="resample.desc", task="missing", size="integer", iters="missing"),
		def = function(x, task, size, iters, ...) {
			make.res.i(x@instance.class, desc=x, size=size, task=NULL)
		}
)

#' @export 
#' @rdname make.res.instance

setMethod(
		f = "make.res.instance",
		signature = c(x="resample.desc", task="learn.task", size="missing", iters="missing"),
		def = function(x, task, size, iters, ...) {
			make.res.i(x@instance.class, desc=x, task=task, blocking=task["blocking"])
		}
)


make.res.i = function(i.class, desc, task=NULL, size=as.integer(NA), blocking=factor(c())) {
  if (!is.null(task)) {
    size = task["size"]
  }
	if (length(blocking) > 1) {
    if (is(desc, "stratcv.desc"))
      stop("Blocking can currently not be mixed with stratification in resampling!")
		levs = levels(blocking)
		size2 = length(levs)
		# create instance for blocks
		inst = new(i.class, desc=desc, size=size2)
		# now exchange block indices with shuffled indices of elements of this block
		f = function(i) sample(which(blocking == levs[i]))
		g = function(inds) Reduce(c, lapply(inds, f))
		inst@inds = lapply(inst@inds, g) 
		inst@size = size
	} else { 
		inst = new(i.class, desc=desc, size=size, task=task)
	}
	return(inst)
}
