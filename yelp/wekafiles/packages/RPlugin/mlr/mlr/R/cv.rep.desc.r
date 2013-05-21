#' @include resample.desc.r
roxygen()


setClass("repcv.desc", 
		contains = c("resample.desc.nonseq")
)                                                     



setMethod(
		f = "initialize",
		signature = signature("repcv.desc"),
		def = function(.Object, iters, reps=10L, aggr1=mean, aggr2=list(mean=mean, sd=sd), ...) {
      ai = function(x, a1, a2, reps, iters) {
        y = split(x, f=rep(1:reps, each=iters))
        y = sapply(y, a1)
        a2(y)
      }
      ais = lapply(aggr2, function(a2) {
          force(a2)
          function(x) ai(x, a1=aggr1, a2=a2, reps=reps, iters=iters)
       }) 
			.Object = callNextMethod(.Object, instance.class="repcv.instance", name="repeated cv", iters=iters,  
        aggr.iter=ais, has.groups=FALSE)
      .Object@props$reps=reps
      return(.Object)
		}
)

#' @rdname to.string

setMethod(
  f = "to.string",
  signature = signature("repcv.desc"),
  def = function(x) {
    return(
      paste(
        x["name"],  " with ", x@iters, " iterations and ", x["reps"] ," repetitions.\n",
        sep=""
      )
    )
  }
)


