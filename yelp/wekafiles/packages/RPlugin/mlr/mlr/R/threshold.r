# return label factor from prob. and threshold

prob.threshold = function(probs, pos, neg, levels, threshold) {
	if (length(threshold) == 0)
		threshold = 0.5
	levs = c(neg, pos)
	if (is.matrix(probs))
		probs = probs[, pos]
	factor(levs[as.numeric(probs > threshold) + 1], levels=levels)
}


