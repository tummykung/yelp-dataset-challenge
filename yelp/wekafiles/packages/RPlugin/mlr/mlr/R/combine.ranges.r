#' Convenience function for \code{\link{tune}} to combine different \code{ranges} lists (i.e. lists of named vectors/lists 
#' of possible hyperparameter values).
#' 
#' @param ... Lists of ranges.
#' @return A list of \code{ranges}
#' @seealso \code{\link{tune}}
#' @export 
#' @title Combine non-orthogonal ranges


combine.ranges <- function(...) {
	rs <- list(...)
	names(rs) <- rep("ranges", length(rs))
	return(rs)
}