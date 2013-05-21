#' @include resample.instance.r
#' @include cv.rep.desc.r
roxygen()



setClass(
		"repcv.instance", 
		contains = c("resample.instance.nonseq")
)                                                     

setMethod(
		f = "initialize",
		signature = signature("repcv.instance"),
		def = function(.Object, desc, size, task) {
			inds = replicate(desc["reps"], make.res.instance("cv", iters=desc["iters"], size=size)@inds, simplify=FALSE)
			inds = Reduce(c, inds)
			callNextMethod(.Object, desc=desc, size=size, inds=inds)
		}
)

setMethod(
  f = "to.string",
  signature = signature("repcv.instance"),
  def = function(x) {
    return(
      paste(
        "Instance for ", x["name"],  " with ", x@desc@iters, " iterations, ", x["reps"], " repetitions and ", x@size, " cases\n",
        paste(capture.output(str(x@inds)), collapse="\n"), 
        "\n", sep=""
      )
    )
  }
)

