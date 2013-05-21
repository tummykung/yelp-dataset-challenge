\name{args.to.control}
\alias{args.to.control}
\title{Split arguments into 'control' and 'other' arguments.}
\usage{args.to.control(control, arg.names, args)
}
\description{Split arguments into 'control' and 'other' arguments.}
\details{Find all elements in list \code{args} whose name is contained in
\code{arg.names} and call function \code{control} on these. The
result of this is returned as the \code{control} element of the
list returned. All remaining elements in \code{args} are returned
as the \code{args} element of the return list.}
\value{List with elements \code{control} and \code{args}.}
\arguments{\item{control}{[function] \cr Function to apply to the elements of
\code{args} named in \code{arg.names}.}
\item{arg.names}{[character] \cr List of argument names to extract
from \code{args}.}
\item{args}{[list] \cr List of named arguments to be split into
control and other arguments.}
}
