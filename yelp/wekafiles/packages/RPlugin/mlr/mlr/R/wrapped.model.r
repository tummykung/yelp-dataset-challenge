# todo: wrapped.model becomes base class, have a normal subclass + one for optimized models???

#' @include object.r
roxygen()
#' @include data.desc.r
roxygen()
#' @include task.desc.r
roxygen()
#' @include learner.r
roxygen()


#' Result from \code{\link{train}}. It internally stores the underlying fitted model,
#' the IDs of the subset used for training, variables used for training and    
#' information about second-level optimization like tuned hyperparameters or selected variables. 
#' 
#' Getter.\cr
#' Note that all getters of \code{\linkS4class{task.desc}} and \code{\linkS4class{data.desc}} can also be used. 
#' 
#' \describe{
#'	\item{learner [{\linkS4class{learner}}]}{Learner that was used to fit the model.}
#'	\item{learner model [any]}{Underlying model from used R package.}
#'	\item{subset [integer]}{Subset used for training.}
#'	\item{fail [NULL | string]}{Generally NULL but if the training failed, the error message of the underlying train function.}
#' }
#' 
#' @title Induced model of learner.
 
setClass(
		"wrapped.model",
		contains = c("object"),
		representation = representation(
				learner = "learner",
				learner.model = "ANY",
				data.desc = "data.desc",
				task.desc = "task.desc",
				subset = "numeric",
				vars = "character",
				time = "numeric"
		)
)


#' @rdname to.string

setMethod(
		f = "to.string",
		signature = signature("wrapped.model"),
		def = function(x) {
			ps = x["learner"]["par.vals"]
			ps = paste(names(ps), ps, sep="=", collapse=" ")
			f = x["fail"]
			f = ifelse(is.null(f), "", paste("Training failed:", f))
			
			return(
					paste(
							"Learner model for ", x@learner["id"], "\n",  
							"Trained on obs: ", length(x@subset), "\n",
							"Hyperparameters: ", ps, "\n",
							f,
							sep=""
					)
			)
		}
)


#' Getter.
#' @rdname wrapped.model-class

setMethod(
		f = "[",
		signature = signature("wrapped.model"),
		def = function(x,i,j,...,drop) {
			args = list(...)
			
			if (i == "fail"){
				if (is(x@learner.model, "learner.failure"))
					return(x@learner.model@msg)
				else
					return(NULL)
			}
			if (i == "opt.result"){
				if (is(x@learner, "opt.wrapper"))
					return(attr(x["learner.model"], "opt.result"))
				else
					return(NULL)
			}
			y = x@task.desc[i]
			if (!is.null(y))
				return(y)
			y = x@data.desc[i]
			if (!is.null(y))
				return(y)
			
			callNextMethod()
		}
)













