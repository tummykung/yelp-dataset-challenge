#' @include base.wrapper.r
roxygen()


#' Wrapper class for learners to handle multi-class problems. 
#' 
#' @exportClass multiclass.wrapper
#' @title Wrapper class for learners to handle multi-class problems.
setClass(
        "multiclass.wrapper",   
        contains = c("base.wrapper"),
        representation = representation(
                codematrix = "matrix",
                method = "function"
        )       
)

#' Constructor.

setMethod(
        f = "initialize",
        signature = signature("multiclass.wrapper"),
        def = function(.Object, learner, id, label, codematrix) {
            .Object = callNextMethod(.Object, learner, id=id, label=label, par.descs=list(), par.vals=list())
            .Object@codematrix = codematrix
            return(.Object)
        }
)

#' @rdname multiclass.wrapper-class

setMethod(
        f = "[",
        signature = signature("multiclass.wrapper"),
        def = function(x,i,j,...,drop) {
            if (i == "multiclass")
                return(TRUE)
            if (i == "probs")
                return(FALSE)
            if (i == "decision")
                return(FALSE)
            if (i == "codematrix")
                return(x@codematrix)
            callNextMethod()
        }
)

#' Fuses a base learner with a multi-class method. Creates a learner object, which can be
#' used like any other learner object. This way learners which can only handle binary classification 
#' will be able to handle multi-class problems too.
#'
#' @param learner [\code{\linkS4class{learner}} or string]\cr 
#'        Learning algorithm. See \code{\link{learners}}.  
#' @param id [string] \cr
#'        Id for resulting learner object. If missing, id of "learner" argument is used.
#' @param label [string] \cr
#'        Label for resulting learner object. If missing, label of "learner" argument is used.
#' @param method [string] \cr
#'        Currently unsupported.
#' @param codematrix [matrix] \cr
#'        ECOC codematrix with entries +1,-1,0. Columns define new binary problems, rows correspond to classes.
#' @param ... [any] \cr
#'        Optional parameters. Not used currently.   
#' 
#' @return \code{\linkS4class{learner}}.
#' 
#' @title Fuse learner with multiclass method.
#' @export
make.multiclass.wrapper = function(learner, id=as.character(NA), label=as.character(NA), method, codematrix, ...) {
    if (is.character(learner))
        learner = make.learner(learner)
    new("multiclass.wrapper", learner=learner, id=id, label=label, codematrix=codematrix)
}


#' @rdname train.learner

setMethod(
        f = "train.learner",
        signature = signature(
                .learner="multiclass.wrapper", 
                .targetvar="character", 
                .data="data.frame", 
                .data.desc="data.desc", 
                .task.desc="task.desc", 
                .weights="numeric", 
                .costs="matrix" 
        ),
        
        def = function(.learner, .targetvar, .data, .data.desc, .task.desc, .weights, .costs,  ...) {   
            cm = .learner["codematrix"]
            y = .data[,.targetvar]
            x = multi.to.binary(y, cm)
            k = length(x$row.inds) 
            levs = .data.desc["class.levels"]
            models = list()
            args = list(...)
            for (i in 1:k) {
                data2 = .data[x$row.inds[[i]], ]
                data2[, .targetvar] = x$targets[[i]] 
                ct = make.task(data=data2, target=.targetvar, positive="1")
                m = train(.learner["learner"], task=ct, par.vals=args)
                models[[i]] = m 
            }
            return(models)
        }
)

#' @rdname pred.learner

setMethod(
        f = "pred.learner",
        signature = signature(
                .learner = "multiclass.wrapper", 
                .model = "wrapped.model", 
                .newdata = "data.frame", 
                .type = "character" 
        ),
        
        def = function(.learner, .model, .newdata, .type, ...) {
            models = .model["learner.model"]
            cm = .model["learner"]["codematrix"]
            k = length(models)
            p = matrix(0, nrow(.newdata), ncol=k)
            # we use hamming decoding here
            for (i in 1:k) {
                m = models[[i]]
                p[,i] = as.integer(as.character(predict(m, newdata=.newdata, ...)["response"]))
            }
            rns = rownames(cm)
            y = apply(p, 1, function(v) {
                # todo: break ties
                #j = which.min(apply(cm, 1, function(z) sum(abs(z - v))))
                d <- apply(cm, 1, function(z) sum(abs(z - v)))
                j <- which(d == min(d))
                j <- sample(rep(j,2), size = 1)
                rns[j]
            })
            as.factor(y)
        }
)   




# Function for Multi to Binary Problem Conversion
multi.to.binary = function(target, codematrix){
    
    if (any(is.na(codematrix)) ) {
        stop("Code matrix contains missing values!")
    }
    levs <- levels(target)
    no.class <- length(levs)
    rns = rownames(codematrix)
    if (is.null(rns) || !setequal(rns, levs)) {
        stop("Rownames of code matrix have to be the class levels!")
    }
    
    binary.targets = as.data.frame(codematrix[target,])
    row.inds = lapply(binary.targets, function(v) which(v != 0))
    names(row.inds) = NULL
    targets = Map(function(y, i) factor(y[i]),
            binary.targets, row.inds)
    
    return(list(row.inds=row.inds, targets=targets))
}

cm.onevsrest = function(data.desc, task.desc) {
    n = data.desc["class.nr"]
    cm = matrix(-1, n, n)
    diag(cm) = 1
    rownames(cm) = data.desc["class.levels"]
    return(cm)
} 

cm.onevsone = function(data.desc, task.desc) {
    n = data.desc["class.nr"]
    cm = matrix(0, n, choose(n, 2))
    combs = combn(n, 2)
    for (i in 1:ncol(combs)) {
        j = combs[,i]
        cm[j, i] = c(1, -1) 
    }
    rownames(cm) = data.desc["class.levels"]
    return(cm)
} 
