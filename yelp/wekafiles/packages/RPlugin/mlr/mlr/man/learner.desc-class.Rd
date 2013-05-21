\name{learner.desc-class}
\alias{learner.desc-class}
\alias{learner.desc}
\title{Description object for learner.}
\description{Description object for learner.}
\details{\code{learner.desc-class}: Getter.\cr

\describe{
\item{numerics [boolean]}{Can numeric inputs be processed?}
\item{factors [boolean]}{Can factor inputs be processed?}
\item{characters [boolean]}{Can character inputs be processed?}
\item{missings [boolean]}{Can missing values be processed?}
\item{weights [boolean]}{Can case weights be used?}
\item{multiclass [boolean]}{Can probabilities be predicted?}
\item{costs [boolean]}{Can misclassification costs be directly used during training?}
\item{probs [boolean]}{Can probabilities be predicted?}
\item{decision [boolean]}{Can probabilities be predicted?}
}

}
\section{Extends}{\code{\linkS4class{object}}}
\section{Methods}{\describe{\item{\code{\link[=[,learner.desc-method]{[}}}{}}}
\alias{[,learner.desc-method}

