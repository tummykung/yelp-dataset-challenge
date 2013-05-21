

#' Implement this for your own sequential resampling.
#' @exportMethod get.train.set
#' @rdname get.train.set
setGeneric(
		name = "get.train.set",
		def = function(x, i) {
			standardGeneric("get.train.set")
		}
)


#' Implement this for your own sequential resampling.
#' @exportMethod get.test.set
#' @rdname get.test.set
setGeneric(
		name = "get.test.set",
		def = function(x, i) {
			standardGeneric("get.test.set")
		}
)

#' @rdname get.train.set
#' @export
setMethod(
		f = "get.train.set",
		signature = signature("resample.instance", "integer"),
		def = function(x, i) {
			return(x@inds[[i]])
		}
)

#' @rdname get.test.set
#' @export
setMethod(
		f = "get.test.set",
		signature = signature("resample.instance", "integer"),
		def = function(x, i) {
			inds = setdiff(1:x["size"], x@inds[[i]])
			list(inds=inds, group=NA)
		}
)

#' Implement this for your own sequential resampling.
#' @exportMethod resample.update
#' @rdname resample.update

setGeneric(
		name = "resample.update",
		def = function(x, task, model, pred) {
			standardGeneric("resample.update")
		}
)



#' @rdname resample.update
#' @export
setMethod(
		f = "resample.update",
		signature = signature("resample.instance", "learn.task", "wrapped.model", "prediction"),
		def = function(x, task, model, pred) {
			return(x)
		}
)

#' Implement this for your own sequential resampling.
#' @exportMethod resample.done
#' @rdname resample.done

setGeneric(
		name = "resample.done",
		def = function(x, task, model, pred) {
			standardGeneric("resample.done")
		}
)


#' @rdname resample.done
#' @export

setMethod(
		f = "resample.done",
		signature = signature("resample.instance"),
		def = function(x, task, model, pred) {
			return(x)
		}
)


