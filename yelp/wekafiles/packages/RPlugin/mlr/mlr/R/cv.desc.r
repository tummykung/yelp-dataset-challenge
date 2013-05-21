#' @include resample.desc.r
roxygen()


setClass("cv.desc", 
		contains = c("resample.desc.nonseq")
)                                                     



setMethod(
		f = "initialize",
		signature = signature("cv.desc"),
		def = function(.Object, iters, ...) {
			callNextMethod(.Object, "cv.instance", "cross-validation", iters, has.groups=FALSE)
		}
)





