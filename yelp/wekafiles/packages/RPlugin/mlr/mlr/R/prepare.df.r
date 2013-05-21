setClass(
		"prepare.control",
		representation = representation(
				props = "list"
		)
)

setMethod(
		f = "initialize",
		signature = signature("prepare.control"),
		def = function(.Object, props) {
			if (missing(props)) {
				props = list(ints.as = "numeric", chars.as = "factor", drop.class.levels=TRUE, impute.inf="maxval")
			}
			.Object@props = props 		
			return(.Object)
		}
)


prep.data = function(is.classif, data, target, excluded=c(), control) {
	
	ints.as = control@props$ints.as
	chars.as = control@props$chars.as
	drop.class.levels = control@props$drop.class.levels
	impute.inf = control@props$impute.inf
	
	if (is.classif && !is.null(data[[target]])) {
		targets = data[, target]
		
		#convert target to factor
		if (!is.factor(targets)) {
			if(is.integer(data[, target]) || is.character(targets) || is.logical(factor)) {
				if (.mlr.local$errorhandler.setup$on.convert.var == "warn")
					warning("Converting target col. to factor.")
				data[, target] = as.factor(targets)
			} else {
				stop("Unsuitable target col. for classification data!")				
			}
		}	
		
		targets = data[, target]
		
		# drop unused class levels
		if (drop.class.levels) {
			before.drop <- levels(targets)
			data[, target] <- targets[, drop=TRUE]
			after.drop <- levels(data[, target])
			if(!identical(before.drop, after.drop)) {
				warning(paste("Empty levels were dropped from class col.:", 
								setdiff(before.drop, after.drop)))
			}	
		}
	}	
	
	cns = colnames(data)
	excluded = c(excluded, target)
	for (i in 1:ncol(data)) {
		cn = cns[i]
		v = data[, i]
		if (!(cn  %in% excluded)) {
			if (ints.as == "numeric" && is.integer(v)) {
				data[,i] = as.numeric(v)
				if (.mlr.local$errorhandler.setup$on.convert.var == "warn")
					warning("Converting integer variable to numeric: ", cn)
			}
			if (ints.as == "factor" && is.integer(v)) {
				data[,i] = as.factor(v)
				if (.mlr.local$errorhandler.setup$on.convert.var == "warn")
					warning("Converting integer variable to factor: ", cn)
			}
			if (chars.as == "factor" && is.character(v)) {
				data[,i] = as.factor(v)
				if (.mlr.local$errorhandler.setup$on.convert.var == "warn")
					warning("Converting char variable to factor: ", cn)
			}
			if (impute.inf == "maxval" && is.numeric(v) && any(is.infinite(v))) {
				v[is.infinite(v)] = sign(v[is.infinite(v)]) * .Machine$double.xmax  
				data[,i] = v
				if (.mlr.local$errorhandler.setup$on.convert.var == "warn")
					warning("Converting inf values to +-.Machine$double.xmax: ", cn)
			}
		}
	}
	
	return(data)    
}



