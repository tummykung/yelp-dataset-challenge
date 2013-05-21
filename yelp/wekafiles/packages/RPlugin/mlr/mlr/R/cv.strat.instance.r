#' @include resample.instance.r
#' @include cv.strat.desc.r
roxygen()



setClass(
  "stratcv.instance", 
  contains = c("resample.instance.nonseq")
)                                                     

setMethod(
  f = "initialize",
  signature = signature("stratcv.instance"),
  def = function(.Object, desc, size, task) {
    if (is.null(task))
      stop("stratcv always needs to be passed the task, otherwise stratification is impossible!")
    if (!task["is.classif"])
      stop("stratcv is currently only supported for classification!")
    y = task["targets"]
    k = desc["iters"]
    # CV on every class
    class.inds = lapply(task["class.levels"], function(x) which(x==y))
    test.inds = lapply(class.inds, function(x) suppressWarnings(split(sample(x), 1:k)))
    # combine them all, so we have the test.inds
    test.inds = Reduce(function(i1, i2) Map(c, i1, i2), test.inds)
    # now shuffle data set and remove test inds
    inds = sample(1:task["size"])
    inds = lapply(test.inds, function(x) setdiff(inds, x))
    callNextMethod(.Object, desc=desc, size=size, inds=inds)
  }
)


