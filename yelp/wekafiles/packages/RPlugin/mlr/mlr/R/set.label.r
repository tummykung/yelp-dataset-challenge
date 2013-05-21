#' Set label of learner object. 
#' 
#' @param learner [\code{\linkS4class{learner}}]\cr 
#'        Learner object.   
#' @param label [string] \cr
#'       New label.
#' 		    
#' @return \code{\linkS4class{learner}} with changed label.
#' @exportMethod set.label
#' @title Set label of learner object.
#' @rdname set.label 

setGeneric(
		name = "set.label",
		def = function(learner, label) {
			standardGeneric("set.label")			
		}
)

#' @rdname set.label 
setMethod(
		f = "set.label",
		
		signature = signature(
				learner="learner", 
				label="character" 
		),
		
		def = function(learner, label) {
			learner@label = label
			return(learner)
		} 
)



