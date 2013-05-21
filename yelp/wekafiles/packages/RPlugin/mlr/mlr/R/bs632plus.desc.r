#' @include resample.desc.r
roxygen()

setClass("bs632plus.desc", 
    contains = c("resample.desc.nonseq")
)                                                     


setMethod(
    f = "initialize",
    signature = signature("bs632plus.desc"),
    def = function(.Object, iters, ...) {
      aggr.group = function(x, g, pred) {
        y1 = pred["truth"]
        y2 = pred["response"]
        grid = expand.grid(y1, y2, KEEP.OUT.ATTRS=FALSE)
        pred2 = make.prediction(data.desc=pred@data.desc, task.desc=pred@task.desc, 
          id=NULL, truth=grid[,1], type="response", y=grid[,2], group=NULL, 
          threshold=as.numeric(NA), 
          time.train=as.numeric(NA), time.predict=as.numeric(NA)) 
        gamma = performance(pred2, measures=colnames(x), aggr="mean")$measures
        sin = as.numeric(x[which(g == "train"),])
        sout = as.numeric(x[which(g == "test"),])
        R = (sout - sin) / (gamma - sin)
        w = 0.632 / (1 - 0.368*R)
        (1-w) * sin + w*sout
      }
      callNextMethod(.Object, "bs632plus.instance", "B632+", iters, has.groups=TRUE, aggr.group=aggr.group)
    }
)


