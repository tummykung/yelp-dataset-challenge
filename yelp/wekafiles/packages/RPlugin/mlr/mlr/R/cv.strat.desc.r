#' @include resample.desc.r
roxygen()


setClass("stratcv.desc", 
  contains = c("resample.desc.nonseq")
)                                                     



setMethod(
  f = "initialize",
  signature = signature("stratcv.desc"),
  def = function(.Object, iters, ...) {
    callNextMethod(.Object, "stratcv.instance", "stratified cross-validation", iters, has.groups=FALSE)
  }
)





