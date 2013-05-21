\name{make.res.desc}
\alias{make.res.desc}
\title{Construct resampling description.}
\usage{make.res.desc(method, iters, ...)
}
\description{Generates a description object for a resampling strategy.}
\value{\code{\linkS4class{resample.desc}}.}
\arguments{\item{method}{[string] \cr
"cv" for cross-validation, "stratcv" for stratified cross-validation,  "repcv" for repeated cross-validation,\cr
"bs" for out-of-bag bootstrap, "bs632" for B632 bootstrap, "bs632plus" for B632+ bootstrap,\cr
"subsample" for subsampling, "holdout" for holdout.}
\item{iters}{[integer] \cr
Number of resampling iterations. Not needed for "holdout".}
\item{...}{[any] \cr
Further parameters for strategies.\cr 
split: Percentage of training cases for "holdout", "subsample".\cr
reps: Repeats for "repcv"}
}
