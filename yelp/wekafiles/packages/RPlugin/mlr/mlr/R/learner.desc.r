#' @include object.r
roxygen()

#' Description object for learner.
#' 
#' Getter.\cr
#' 
#' \describe{
#'  \item{numerics [boolean]}{Can numeric inputs be processed?}
#'  \item{factors [boolean]}{Can factor inputs be processed?}
#'  \item{characters [boolean]}{Can character inputs be processed?}
#'  \item{missings [boolean]}{Can missing values be processed?}
#'  \item{weights [boolean]}{Can case weights be used?}
#'  \item{multiclass [boolean]}{Can probabilities be predicted?}
#'  \item{costs [boolean]}{Can misclassification costs be directly used during training?}
#'  \item{probs [boolean]}{Can probabilities be predicted?}
#'  \item{decision [boolean]}{Can probabilities be predicted?}
#' }
#' @exportClass learner.desc
#' @title Description object for learner. 


setClass(
		"learner.desc",
		contains = c("object"),
		representation = representation(
				props = "list"
		)
)



#' Constructor.
setMethod(
		f = "initialize",
		signature = signature("learner.desc"),
		def = function(.Object, missings, numerics, factors, characters, weights) {
			if (missing(missings))
				return(.Object)
			.Object@props$missings = missings 
			.Object@props$numerics = numerics 
			.Object@props$factors = factors 
			.Object@props$characters = characters 
			.Object@props$weights = weights
			return(.Object)
		}
)

#' @rdname learner.desc-class
setMethod(
		f = "[",
		signature = signature("learner.desc"),
		def = function(x,i,j,...,drop) {
			return(x@props[[i]])
		}
)



setClass(
		"learner.desc.regr",
		contains = c("learner.desc")
)


#' Constructor.
setMethod(
		f = "initialize",
		signature = signature("learner.desc.regr"),
		def = function(.Object, missings, numerics, factors, characters, weights) {
			callNextMethod(.Object, missings, numerics, factors, characters, weights)
		}
)



setClass(
		"learner.desc.classif",
		contains = c("learner.desc")
)


#' Constructor.
setMethod(
		f = "initialize",
		signature = signature("learner.desc.classif"),
		def = function(.Object, missings, numerics, factors, characters, weights, oneclass, twoclass, multiclass, probs, decision, costs) {
			.Object@props$multiclass = multiclass 
			.Object@props$probs = probs 
			.Object@props$decision = decision 
			.Object@props$costs = costs 
			callNextMethod(.Object, missings, numerics, factors, characters, weights)
		}
)

