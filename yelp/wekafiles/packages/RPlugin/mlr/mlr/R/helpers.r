c.factor = function(..., recursive=FALSE) {
	args <- list(...)
	for (i in seq_along(args))
    if (!is.factor(args[[i]]))
      args[[i]] = as.factor(args[[i]])
  ## The first must be factor otherwise we wouldn't be inside
  ## c.factor, its checked anyway in the line above.
	newlevels = sort(unique(unlist(lapply(args, levels))))
	ans = unlist(lapply(args, function(x) {
						m = match(levels(x), newlevels)
						m[as.integer(x)]
					}))
	levels(ans) = newlevels
	class(ans) = "factor"
	return(ans)
}


## do lapply recursively on deep lists
## FIXME: Use rapply instead? Possibly not useful because rapply does
##   not limit descend depth. Investigate if rec.lapply becomes a
##   bottleneck.
rec.lapply = function(xs, fun, depth=Inf) {
	if (!is.list(xs) || is.data.frame(xs) || depth==0) {
		return(fun(xs))
	}
	lapply(xs, function(x) rec.lapply(x, fun, depth-1))
}


# inserts elements from x2 into x1, overwriting elements of equal names
# if el.names contains names which are nor present in x2, they are disregarded
insert = function(xs1, xs2, el.names) {
  if (missing(el.names)) {
    xs1[names(xs2)] <- xs2
  } else {
    el.names = intersect(el.names, names(xs2))
    xs1[el.names] <- xs2[el.names]
  }
	return(xs1)
}

# inserts elements from x2 into x1, only if names in x2 are already present in x1 
insert.matching = function(xs1, xs2) {
	ns = intersect(names(xs1), names(xs2))
	xs1[ns] = xs2[ns]
	return(xs1)
}


##' Split arguments into 'control' and 'other' arguments.
##'
##' Find all elements in list \code{args} whose name is contained in
##' \code{arg.names} and call function \code{control} on these. The
##' result of this is returned as the \code{control} element of the
##' list returned. All remaining elements in \code{args} are returned
##' as the \code{args} element of the return list.
##'
##' @param control [function] \cr Function to apply to the elements of
##'   \code{args} named in \code{arg.names}.
##'
##' @param arg.names [character] \cr List of argument names to extract
##'   from \code{args}.
##'
##' @param args [list] \cr List of named arguments to be split into
##'   control and other arguments.
##'
##' @return List with elements \code{control} and \code{args}.
##' @export
args.to.control = function(control, arg.names, args) {
	# put stuff into special list and remove it from args
	ctrl.args = insert(list(), args, arg.names)
	ctrl = do.call(control, ctrl.args)
	args[arg.names] = NULL
	return(list(control=ctrl, args=args))
}


check.list.type = function(xs, type, name=deparse(substitute(xs))) {
  ## FIXME: Better use inherits like this?
  ##   sapply(xs, function(x) inherits(x, type))
  
	fs = lapply(type, function(tt) switch(tt,
    character=is.character,                          
    numeric=is.numeric,
    logical=is.logical,
    integer=is.integer,
    list=is.list,
    data.frame=is.data.frame,
    function(x) is(x, tt)
    ))
	types = paste(type, collapse=", ")	
	all(sapply(seq_along(xs), function(i) {
				x = xs[[i]]
				ys = sapply(fs, function(f) f(x))
				if(!any(ys))
					stop("List ", name, " has element of wrong type ", class(x), " at position ", i, ". Should be: ", types)
				any(ys)
	}))
}


##' Returns TRUE if all entries in the name attribute of \code{xs} valid names.
all.names = function(xs) {
	ns = names(xs)
	(length(xs) == 0) || (!is.null(ns) && !any(is.na(ns)) && !any(ns == ""))
}
 
vote.majority = function(x) {
	tt = table(x)
	y = seq_along(tt)[tt == max(tt)]
	if (length(y) > 1L) 
		y = sample(y, 1L)
	names(tt)[y]
}

# selects the maximal name of the maximal element of a numerical vector - breaking ties at random
vote.max.val = function(x, names=names(x)) {
	y = seq_along(x)[x == max(x)]
	if (length(y) > 1L) 
		y = sample(y, 1L)
	return(names[y])
}


# returns first non null el. 
coalesce = function (...) {
	l <- list(...)
	isnull <- sapply(l, is.null)
	l[[which.min(isnull)]]
}

## FIXME: 20100925 - Not used and possibly nonsense...
## list2dataframe = function(xs, rownames=NULL) {
##	ys = as.data.frame(do.call(rbind, xs))
##	rownames(ys) = rownames
##	return(ys)
## }

path2dataframe = function(path) {
	p = path[[1]]
	cns = c(names(p$par), "threshold", names(p$perf), "evals", "event", "accept")
	df = matrix(0, length(path), length(cns))
	colnames(df) = cns
	n = length(p$par)
	m = length(p$perf)
	k = ncol(df)
	df = as.data.frame(df)
	df$event = as.character(df$event)
	for (i in 1:length(path)) {
		p = path[[i]]
		df[i, 1:n] = unlist(p$par)  
		df[i, (n+1):(k-2)] = c(p$threshold, unlist(p$perf), p$evals)  
		df[i, k-1] = p$event  
		df[i, k] = p$accept  
	}
	return(df)
}

check.getter.args = function(x, arg.names, j, ...) {
	args = list(...)
	ns = names(args)
	for (i in seq_along(args)) {
		n = ns[i]
		a = args[[i]]
		# condition because of spurious extra arg (NULL) bug in "["
		if ( !(is.null(a) && (is.null(n) || length(a) == 0)) ) {
			if (is.null(n) || length(a) == 0)
				stop("Using unnamed extra arg ", a, " in getter of ", class(x), "!")
			if (!(n %in% arg.names))
				stop("Using unallowed extra arg ", paste(n, a, sep="="), " in getter of ", class(x), "!")
		}
	}
}

require.packs = function(packs, for.string) {
  # this should be a bit faster...
  packs.ok = sapply(packs, function(x) paste("package", x, sep = ":") %in% search())
  packs = packs[!packs.ok]
  packs.ok = sapply(packs, function(x) require(x, character.only = TRUE))
	if (length(packs.ok) == 0)
		packs.ok = TRUE
	if(!all(packs.ok)) {
		ps = paste(packs[!packs.ok], collapse=" ")
		stop(paste("For", for.string, "please install the following packages:", ps))
    ## DOIT: Possibly add option to run install.packages() if interactive() is TRUE?
    ##  Check adverse effects regarding parallelization.
	}
	return(packs.ok)
}

##' Check if \code{e1} and \code{e2} are equal ignoring such fine
##' points as \code{1 != 1L}.
almost.equal <- function(e1, e2) {
  isTRUE(all.equal(e1, e2))
}

hyper.par.val.to.name = function(par.name, par.val, learner) {
  pds = learner["par.descs"]
  pd = pds[[par.name]]
  if(!is.null(pd) && is(pd, "par.desc.disc")) {
    vals = pd["vals"]
    i = which(sapply(vals, function(x) almost.equal(par.val, x)))
    return(names(vals)[i])
  }
  return(par.val)
}

# converts a row of a data.frame to a list
# - factors are converted to chars
data.frame.row.to.list = function(x, i) {
  x = as.list(x[i,])
  x = lapply(x, function(y) if(is.factor(y)) as.character(y) else y)
}


