\name{make.res.instance}
\alias{make.res.instance-methods}
\alias{make.res.instance}
\title{Construct resampling instance}
\description{Generates an instance object for a resampling strategy.}
\value{\code{make.res.instance-methods}: A \code{\linkS4class{resample.instance}} object.

}
\arguments{\item{x}{[string or  \code{\linkS4class{resample.desc}}] \cr
"cv" for cross-validation, "stratcv" for stratified cross-validation,  "repcv" for repeated cross-validation,\cr
"bs" for out-of-bag bootstrap, "bs632" for B632 bootstrap, "bs632plus" for B632+ bootstrap,\cr
"subsample" for subsampling, "holdout" for holdout.}
\item{task}{[\code{\link{integer}}] \cr
Data of task to resample from. Prefer to pass this instead of \code{size}.}
\item{size}{[\code{\link{integer}}] \cr
Size of the data set to resample.}
\item{iters}{[integer] \cr
Number of resampling iterations. Not needed for "holdout".}
\item{...}{[any] \cr
Further parameters for strategies.\cr 
split: Percentage of training cases for "holdout", "subsample".\cr
reps: Repeats for "repcv"}
}
\alias{make.res.instance,character,missing,integer,integer-method}
\alias{make.res.instance}
\alias{make.res.instance,character,learn.task,missing,integer-method}
\alias{make.res.instance}
\alias{make.res.instance,resample.desc,missing,integer,missing-method}
\alias{make.res.instance}
\alias{make.res.instance,resample.desc,learn.task,missing,missing-method}
\alias{make.res.instance}
