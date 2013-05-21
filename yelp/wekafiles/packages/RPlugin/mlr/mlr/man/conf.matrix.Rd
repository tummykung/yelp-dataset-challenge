\name{conf.matrix}
\alias{conf.matrix}
\title{Confusion matrix.}
\usage{conf.matrix(result, relative=FALSE)
}
\description{Calculates confusion matrix for (possibly resampled) prediction.}
\value{A confusion matrix.}
\seealso{\code{\link[klaR]{errormatrix}}}
\arguments{\item{result}{[\code{\linkS4class{prediction}}] \cr
Result of a prediction.}
\item{relative}{[logical] \cr 
If TRUE rows are normalized to show relative frequencies.}
}
