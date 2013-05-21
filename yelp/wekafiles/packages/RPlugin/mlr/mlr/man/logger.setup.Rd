\name{logger.setup}
\alias{logger.setup}
\title{Logger setup.}
\usage{logger.setup(console=TRUE, file=NA, level, sublevel=NA)
}
\description{Sets up the logging system of mlr.}
\value{NULL.}
\arguments{\item{console}{[boolean] \cr
Should output be printed to R console?}
\item{file}{[string] \cr 
Path to file to redirect output into.}
\item{level}{[character] \cr 
Which logging levels should be printed: 'error', 'warn', 'info', 'debug'. Default is 'info'.}
\item{sublevel}{[character] \cr 
Which logging sublevels should be printed. Default is NA which means all logging of the selected main level is printed.
Currently for 'debug' are available: 'train', 'predict' and 'parallel'.     	
Currently for 'info' are available: 'tune'.}
}
