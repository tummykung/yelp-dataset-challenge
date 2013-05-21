#' @include task.learn.r
roxygen()

#' General description object for a classification experiment.   
#' Instantiate it by using its factory method.
#' 
#' @exportClass classif.task
#' @title Classification task.
#' @seealso \code{\link{make.task}}


setClass(
		"classif.task",
		contains = c("learn.task")
)



#---------------- constructor---- -----------------------------------------------------

#' Constructor.
#' @title classif.task constructor

setMethod(
		f = "initialize",
		signature = signature("classif.task"),
		def = function(.Object, id, label, target, data, excluded, weights, blocking, costs, positive) {
			if (missing(data))
				return(.Object)
			
			prep.ctrl = new("prepare.control")
			data = prep.data(TRUE, data, target, excluded, prep.ctrl)			
			dd = new("data.desc", data=data, target=target, excluded=excluded, prepare.control=prep.ctrl)
			n = dd["class.nr"]
			levs = dd["class.levels"]
			
			# init positive
			pos = positive 
			neg = as.character(NA)
			if (n == 1) {
				if (is.na(pos)) {
					pos = levs[1]
				} else {
					if (!(pos %in% levs))
						stop(paste("Trying to set a positive class", pos, "which is not a value of the target variable:", paste(levs, collapse=",")))
				}
				neg = paste("not_", pos)
			} else if (n == 2) {
				if (is.na(pos)) {
					pos = levs[1] 					
				}
				else {
					if (!(pos %in% levs))
						stop(paste("Trying to set a positive class", pos, "which is not a value of the target variable:", paste(levs, collapse=",")))
				}
				neg = setdiff(levs, pos)
			} else {
				if (!is.na(pos))
					stop("Cannot set a positive class for a multiclass problem!")
			}
			
			# check costs if passed
			if (!all(dim(costs) == 0)) {
				if (!is.matrix(costs))
					stop("costs has to be a matrix!")
				if (any(dim(costs) != n))
					stop("Dimensions of costs has to be the same as number of classes!")
				rns = rownames(costs)
				cns = colnames(costs)
				if (!setequal(rns, levs) || !setequal(cns, levs))
					stop("Row and column names of cost matrix have to equal class levels!")
			}			
			hw = length(weights) > 0
			hb = length(blocking) > 0
			td = new("task.desc", task.class="classif.task", id=id, label=label, has.weights=hw, has.blocking=hb,
							costs=costs, positive=pos, negative=neg)			
			
			callNextMethod(.Object, data=data, weights=weights, blocking=blocking, data.desc=dd, task.desc=td)
		}
)


#' @rdname to.string
setMethod(
		f = "to.string",
		signature = signature("classif.task"),
		def = function(x) {
			di = paste(capture.output(x["class.dist"]), collapse="\n")
			return(
					paste(
							"Classification problem ", x["id"], "\n",
							"Features Nums:", x["n.num"], " Factors:", x["n.fact"], " Chars:", x["n.char"], "\n",
							"Observations: ", x["size"] , "\n",
							"Missings: ", x["has.missing"], "\n", 
							ifelse(x["has.missing"], paste("in", x["rows.with.missing"], "observations and", x["cols.with.missing"], "features\n"), ""), 
							"Classes: ", x["class.nr"], "\n",
							di, "\n",
							ifelse(x["is.binary"], paste("Positive class:", x["positive"], "\n"), ""),
							sep=""
					)
			)
		}
)


