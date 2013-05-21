#' @include subsample.desc.r
roxygen()


setClass("holdout.desc", 
		contains = c("subsample.desc")
)               



setMethod(
		f = "initialize",
		signature = signature("holdout.desc"),
		def = function(.Object, iters, split=2/3, ...) {
			callNextMethod(.Object, split=split, iters=1L)
		}
)

