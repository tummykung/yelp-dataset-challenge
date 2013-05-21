check.ranges <- function(ranges) {
	ns <- names(ranges)
	if(any(is.na(ns) | ns == "")) {
		stop("All element of a ranges list have to be named!")
	}
}