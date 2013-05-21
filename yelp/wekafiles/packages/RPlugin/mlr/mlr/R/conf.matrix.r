#' Calculates confusion matrix for (possibly resampled) prediction. 
#' 
#' @param result [\code{\linkS4class{prediction}}] \cr
#'   Result of a prediction.
#' @param relative [logical] \cr 
#' 	If TRUE rows are normalized to show relative frequencies.
#' 
#' @return A confusion matrix.
#' 
#' @export
#' 
#' @seealso \code{\link[klaR]{errormatrix}}
#' 
#' @title Confusion matrix.


conf.matrix = function(result, relative=FALSE) {
	return(errormatrix(result["truth"], result["response"], relative=relative))
}
