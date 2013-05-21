\name{measures}
\alias{measures}
\title{Performance measures.}
\description{Performance measures can always be passed as a single string (name of a single measure), a character vector of multiple names of measures or 
a list containing string names of measures and your on performance measures as function objects. The latter ones should 
be named list elements.\cr}
\details{Classification: 
\itemize{ 
\item{\bold{mmce}}{\cr Mean misclassification error}
\item{\bold{acc}}{\cr Accuracy}
\item{\bold{costs}}{\cr Misclassification costs according to cost matrix}
\item{\bold{tp}}{\cr True positives}
\item{\bold{tpr, hit-rate, recall}}{\cr True positive rate}
\item{\bold{fp, false-alarm}}{\cr False positives}
\item{\bold{fpr, false-alarm-rate, fall-out}}{\cr False positive rate}
\item{\bold{tn, correct-rejection}}{\cr True negatives}
\item{\bold{tnr, specificity}}{\cr True negative rate}
\item{\bold{fn, miss}}{\cr False negatives}
\item{\bold{fnr}}{\cr False negative rate}
\item{\bold{ppv, precision}}{\cr Positive predictive value}
\item{\bold{npv}}{\cr Negative predictive value}
\item{\bold{fdr}}{\cr False discovery rate}
\item{\bold{f1}}{\cr F1 measure}
\item{\bold{mcc}}{\cr Matthews correlation coefficient}
\item{\bold{gmean}}{\cr G-mean, geomentric mean of recall and specificity.}
\item{\bold{gpr}}{\cr Geometric mean of precision and recall.}
\item{\bold{auc}}{\cr Area under the curve.}

\item{\bold{time.train}}{\cr Time of fitting the model}
\item{\bold{time.predict}}{\cr Time of predicting test set}
\item{\bold{time}}{\cr time.train + train.predict}
}

Regression:
\itemize{ 
\item{\bold{sse}}{\cr Sum of squared errors}
\item{\bold{mse}}{\cr Mean of squared errors}
\item{\bold{medse}}{\cr Median of squared errors}
\item{\bold{sae}}{\cr Sum of absolute errors}
\item{\bold{mae}}{\cr Mean of absolute errors}
\item{\bold{medae}}{\cr Median of absolute errors}

\item{\bold{time.train}}{\cr Time of fitting the model}
\item{\bold{time.predict}}{\cr Time of predicting test set}
\item{\bold{time}}{\cr time.train + train.predict}
}}
