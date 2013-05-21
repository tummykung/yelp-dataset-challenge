#' @include object.r
roxygen()
#' @include learner.desc.r
roxygen()

# todo supports getters
# hyperpars getter, read all getters

#' Abstract base class for learning algorithms.
#'  
#' Getter.\cr
#' Note that all getters of \code{\linkS4class{learner.desc}} can also be used. 
#' 
#' \describe{
#'  \item{is.classif [boolean]}{Is this learner for classification tasks?}
#'  \item{is.regr [boolean]}{Is this learner for regression tasks?}
#'  \item{id [string]}{Id string of learner.}
#'	\item{label [string]}{Label string of learner.}
#' 	\item{pack [string]}{Package were underlying learner is implemented.}
#'	\item{desc [\code{\linkS4class{learner.desc}}]}{Properties object to describe functionality of the learner.}
#' 	\item{par.vals [list]}{List of fixed hyperparameters and respective values for this learner.}
#' 	\item{par.vals.name [character]}{Names of currently fixed hyperparameters.}
#' 	\item{par.descs [list]}{Named list of \code{\linkS4class{par.desc}} description objects for all possible hyperparameters for this learner.}
#' 	\item{par.descs.name [character]}{Names of all hyperparameters for which description objects exist.}
#' 	\item{par.descs.when [character]}{Named character vector. Specifies when a cetrain hyperparameter is used. Possible entries are 'train', 'predict' or 'both'.}
#'  \item{predict.type [character]}{What should be predicted: 'response', 'prob' or 'decision'.}
#'  \item{predict.threshold [character]}{Threshold to produce class labels if type is not "response".} 
#' }
#' @exportClass learner
#' @title Base class for inducers. 

setClass(
		"learner",
		contains = c("object"),
		representation = representation(
				id = "character",
				label = "character",
				pack = "character",
				desc = "learner.desc",
				predict.type = "character",
				predict.threshold = "numeric",					
				par.descs = "list",
				par.vals = "list"
		)		
)


#' Constructor.
setMethod(
		f = "initialize",
		signature = signature("learner"),
		def = function(.Object, id, label, pack, desc, par.descs, par.vals) {			
			if (missing(desc))
				return(.Object)
			if (missing(id))
				id = as.character(class(.Object))
			if (missing(label))
				label = id
			.Object@id = id
			.Object@label = label
			.Object@pack = pack
			.Object@desc = desc
			.Object@predict.type = "response"
			require.packs(pack, for.string=paste("learner", id))
			if (missing(par.descs))
				par.descs = list()
			.Object@par.descs = par.descs
			callNextMethod(.Object)
			if (!missing(par.vals))
				.Object = set.hyper.pars(.Object, par.vals=par.vals)
			return(.Object)
		}
)


#' Getter.
#' @rdname learner-class

setMethod(
		f = "[",
		signature = signature("learner"),
		def = function(x,i,j,...,drop) {
			check.getter.args(x, c("par.when", "par.top.wrapper.only"), j, ...)
			
			if (i == "probs") {
				return(ifelse(x["is.regr"], F, x@desc["probs"]))
			}
			if (i == "decision") {
				return(ifelse(x["is.regr"], F, x@desc["decision"]))
			}
			if (i == "multiclass") {
				return(ifelse(x["is.regr"], F, x@desc["multiclass"]))
			}
			if (i == "missings") {
				return(x@desc["missings"])
			}
			if (i == "costs") {
				return(ifelse(x["is.regr"], F, x@desc["costs"]))
			}
			if (i == "weights") {
				return(x@desc["weights"])
			}
			if (i == "numerics") {
				return(x@desc["numerics"])
			}
			if (i == "factors") {
				return(x@desc["factors"])
			}
			if (i == "characters") {
				return(x@desc["characters"])
			}
      
      if (i == "par.descs") {
        pds = x@par.descs
        names(pds) = sapply(pds, function(y) y@par.name)
        return(pds)
      } 
			if (i == "par.descs.name") 
				return(sapply(x@par.descs, function(y) y@par.name))
			if (i == "par.descs.when") {
				w=sapply(x@par.descs, function(y) y@when)
				names(w) = x["par.descs.name", ...]
				return(w)
			}
			
			args = list(...)
			par.when = args$par.when
			if(is.null(par.when)) par.when = c("train", "predict", "both")
			ps = x@par.vals
			ns = names(ps)
			w = x["par.descs.when"]

			if (i == "par.vals")  {
				return( ps[ (w[ns] %in% par.when) | (w[ns] == "both") ] ) 
			}			
			if (i == "par.vals.name")  {
				return(names(x["par.vals", ...]))
			}
			callNextMethod()
		}
)


#' @rdname to.string
setMethod(f = "to.string",
          signature = signature("learner"),
          def = function(x) {
            hps = x["par.vals"]
            hps.ns = names(hps)
            hps = Map(function(n, v) hyper.par.val.to.name(n,v,x), hps.ns, hps)
            hps = paste(hps.ns, hps, sep="=", collapse=" ")
            is.classif = x["is.classif"]
            type = if (is.null(is.classif))
              "Unknown"
            else if (is.classif)
              "Classification"
            else
              "Regression"
            pack = paste(x["pack"], collapse=",")
            return(paste(
                         ##todo regression. also check when applied to task!!
                         type, " learner ", x["id"], " from package ", pack, "\n\n",
                         "Supported features Nums:", x["numerics"],
                         " Factors:", x["factors"],
                         " Chars:", x["characters"], "\n",
                         "Supports missings: ", x["missings"], "\n", 
                         "Supports weights: ", x["weights"], "\n", 
                         "Supports multiclass: ", x["multiclass"], "\n",
                         "Supports probabilities: ", x["probs"], "\n", 
                         "Supports decision values: ", x["decision"], "\n", 
                         "Supports costs: ", x["costs"], "\n", 
                         "Hyperparameters: ", hps, "\n",
                         sep =""					
                         ))
          })
