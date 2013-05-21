MLR seems to not be available from R-Forge. Windows and Linux/Mac versions can be downloaded from:

http://www.statistik.uni-dortmund.de/~bischl/

mlr_0.3.1206 was the last one that could be installed via install.packages("mlr", repos = "http://R-Forge.R-project.net") for R version 2.14.x.

The zip (Windows) or tar.gz (Linux/Mac) mlr package can be installed with R CMD INSTALL <path to zip/tar.gz>. There are a number of packages that mlr depends on that will have to be installed manually via the R console:

e1071
reshape
plyr
klaR
abind
digest
