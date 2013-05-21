#' Set id of learner object. 
#' 
#' @param learner [\code{\linkS4class{learner}}]\cr 
#'        Learner object.   
#' @param id [string] \cr
#'       New id.
#' 		    
#' @return \code{\linkS4class{learner}} with changed id.
#' @exportMethod set.id
#' @title Set id of learner object.
#' @rdname set.id 

setGeneric(
		name = "set.id",
		def = function(learner, id) {
			standardGeneric("set.id")			
		}
)

#' @rdname set.id 
setMethod(
		f = "set.id",
		
		signature = signature(
				learner="learner", 
				id="character" 
		),
		
		def = function(learner, id) {
			learner@id = id
			return(learner)
		} 
)



