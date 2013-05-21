\name{prediction-class}
\alias{prediction-class}
\alias{prediction}
\title{Prediction.}
\description{Result from \code{\link{predict}}. 
Use \code{as.data.frame} to access all information in a convenient format.}
\details{\code{prediction-class}: Getter.\cr

\describe{
\item{type [string]}{Type set in predict function: "response", "prob", or "decision".}
\item{id [numeric]}{Vector of index numbers of predicted cases from the task.}
\item{response [numeric | factor]}{Predicted response values.}
\item{truth [numeric | factor]}{True target values.}
\item{prob [numeric | matrix] Optional parameters: class}{Predicted probabilities. For binary class. only the probabilities for the positive class are returned.}
\item{decision [matrix]}{Predicted decision values.}
\item{group [factor]}{Grouping of predcitions. Rarely used, if not present NULL is returned.}
\item{threshold [numeric]}{Threshold set in predict function.}
\item{has.groups [boolean]}{Is special grouping used in order to aggregate predictions differently?}
}

}
\seealso{\code{\link{performance}}}
\section{Extends}{\code{\linkS4class{object}}}
\section{Methods}{\describe{\item{\code{\link[=as.ROCR.preds,prediction-method]{as.ROCR.preds}}}{}\item{\code{\link[=resample.update,resample.instance,learn.task,wrapped.model,prediction-method]{resample.update}}}{}\item{\code{\link[=performance,prediction,list,list,list-method]{performance}}}{}\item{\code{\link[=[,prediction-method]{[}}}{\code{signature( = "prediction")}: Getter.}\item{\code{\link[=as.data.frame,prediction-method]{as.data.frame}}}{\code{signature( = "prediction")}: Convert to data.frame}\item{\code{\link[=to.string,prediction-method]{to.string}}}{}}}
\alias{[,prediction-method}

\alias{as.data.frame,prediction-method}
\alias{as.data.frame}
