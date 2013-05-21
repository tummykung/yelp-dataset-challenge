\name{losses}
\alias{losses}
\title{Loss functions.}
\description{Losses can always be passed as a single string (name of a single loss), a character vector of multiple names of losses or 
a list containing string names of losses and your own loss functions as function objects. The latter ones should 
be named list elements.}
\details{Classification: 
\itemize{ 
\item{\bold{zero-one}}{\cr Zero-one loss}
\item{\bold{costs}}{\cr Misclassification costs according to cost matrix}
}

Regression: 
\itemize{ 
\item{\bold{squared}}{\cr Squared error}
\item{\bold{abs}}{\cr Absolute error}
\item{\bold{residual}}{\cr Signed residual error}
}}
