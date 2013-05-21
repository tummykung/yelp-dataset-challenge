#' @include resample.instance.r
#' @include bs.desc.r
roxygen()



setClass(
		"bs.instance", 
		contains = c("resample.instance.nonseq"))                                                     


setMethod(
  f = "initialize",
  signature = signature("bs.instance"),
  def = function(.Object, desc, size, task) {
	inds = boot(1:size, R=desc["iters"], function(data,inds) inds)$t
	inds = as.list(as.data.frame(t(inds)))
	names(inds) = NULL
	callNextMethod(.Object, desc=desc, size=size, inds=inds)
  }
)


