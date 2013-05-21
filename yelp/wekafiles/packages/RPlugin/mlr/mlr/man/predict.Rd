\name{predict}
\alias{predict,wrapped.model-method}
\alias{predict}
\title{Predict new data.}
\description{Predict the target variable of new data using a fitted model. If the type is set to "prob" or "decision"
probabilities or decision values will be stored in the resulting object. The resulting class labels are 
the classes with the maximum values or thresholding can also be used.}
\value{\code{\linkS4class{prediction}}.}
\seealso{\code{\link{train}}}
\arguments{\item{object}{[\code{\linkS4class{wrapped.model}}] \cr 
Wrapped model, trained from a learn task.}
\item{task}{[\code{\linkS4class{learn.task}}]\cr 
Specifies learning task. If this is passed, data from this task is predicted.}
\item{subset}{[integer] \cr 
Index vector to subset the data in the task to use for prediction.}
\item{newdata}{[\code{\link{data.frame}}] \cr 
New observations which should be predicted. Alternatively pass this instead of task.}
\item{type}{[string] \cr
Classification: "response" | "prob" | "decision", specifying the type to predict.
Default is "response". "decision" is experimental.
Ignored for regression.}
\item{threshold}{[numeric] \cr
Threshold to produce class labels if type is not "response". 
Currently only supported for binary classification and type="prob", where it represents the required predicted probability
for the positive class, so that a positive class is predicted as "response".
Default is 0.5 for type="prob".
Ignored for regression.}
\item{group}{[factor] \cr
Only for internal use! 
Default is NULL.}
}
