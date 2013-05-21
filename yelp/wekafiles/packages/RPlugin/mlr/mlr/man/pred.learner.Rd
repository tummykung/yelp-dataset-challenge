\name{pred.learner}
\alias{pred.learner,classif.ada,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\title{pred.learner,classif.ada,wrapped.model,data.frame,character-method}
\alias{pred.learner,classif.adaboost.M1,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,base.wrapper,wrapped.model,data.frame,ANY-method}
\alias{pred.learner}
\alias{pred.learner,classif.blackboost,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.blackboost,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.ctree,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.earth,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.gbm,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.gbm,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.glmboost,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.grplasso,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.J48,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.JRip,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.kknn,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.kknn,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,regr.km,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.ksvm,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.ksvm,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,regr.lasso,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.lda,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.lm,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.loclda,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.logreg,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.lssvm,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.lvq1,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.mars,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.mda,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,multiclass.wrapper,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.multinom,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.naiveBayes,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.nnet,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.nnet,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.OneR,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.PART,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.pcr,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.lpsvm,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner-methods}
\alias{pred.learner}
\description{Mainly for internal use. Predicts new data with wrapped.model. 
You have to implement this method if you want to add another learner to this package.}
\value{\code{pred.learner-methods}: Model of the underlying learner.

}
\arguments{\item{.learner}{[\\code{\\linkS4class{learner}}] \cr  
Wrapped learner from this package.}
\item{.model}{[\code{\link{character}}] \cr
Model produced by training.}
\item{.newdata}{[\code{\link{data.frame}}] \cr
New data to predict.}
\item{type}{[\code{\link{character}}] \cr 
Specifies the type of predictions - either probability ("prob") or class ("class").
Ignore this if it is not classification or the learner does not support probabilities.}
\item{...}{[any] \cr
Additional parameters, which need to be passed to the underlying train function.}
}
\alias{pred.learner,preproc.wrapper,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.qda,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.randomForest,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.randomForest,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.rda,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.ridge,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.rpart,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,regr.rpart,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,regr.rsm,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,regr.rvm,wrapped.model,data.frame,missing-method}
\alias{pred.learner}
\alias{pred.learner,classif.sda,wrapped.model,data.frame,character-method}
\alias{pred.learner}
\alias{pred.learner,classif.svm,wrapped.model,data.frame,character-method}
\alias{pred.learner}
