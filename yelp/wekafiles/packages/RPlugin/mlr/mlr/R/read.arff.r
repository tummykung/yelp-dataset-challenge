read.arff = function (file, remove=character(0)) {
	if (is.character(file)) {
		file <- file(file, "r")
		on.exit(close(file))
	}
	if (!inherits(file, "connection")) 
		stop("Argument 'file' must be a character string or connection.")
	if (!isOpen(file)) {
		open(file, "r")
		on.exit(close(file))
	}
	col_names <- NULL
	col_types <- NULL
	col_dfmts <- character()
	line <- readLines(file, n = 1L)
	while (length(line) && regexpr("^[[:space:]]*@(?i)data", 
			line, perl = TRUE) == -1L) {
		if (regexpr("^[[:space:]]*@(?i)attribute", line, perl = TRUE) > 
				0L) {
			con <- textConnection(line)
			line <- scan(con, character(), quiet = TRUE)
			close(con)
			if (length(line) < 3L) 
				stop("Invalid attribute specification.")
			col_names <- c(col_names, line[2L])
			if ((type <- tolower(line[3L])) == "date") {
				col_types <- c(col_types, "character")
				col_dfmts <- c(col_dfmts, if (length(line) > 
										3L) foreign:::ISO_8601_to_POSIX_datetime_format(line[4L]) else "%Y-%m-%d %H:%M:%S")
			}
			else if (type == "relational") 
				stop("Type 'relational' currently not implemented.")
			else {
				type <- sub("\\{.*", "factor", type)
				type <- sub("string", "character", type)
				type <- sub("real", "numeric", type)
				col_types <- c(col_types, type)
				col_dfmts <- c(col_dfmts, NA)
			}
		}
		line <- readLines(file, n = 1L)
	}
	if (length(line) == 0L) 
		stop("Missing data section.")
	if (is.null(col_names)) 
		stop("Missing attribute section.")
	if (length(col_names) != length(grep("factor|numeric|character|integer", 
					col_types))) 
		stop("Invalid type specification.")
	data <- read.table(file, sep = ",", na.strings = "?", colClasses = col_types, 
			comment.char = "%")
	if (any(ind <- which(!is.na(col_dfmts)))) 
		for (i in ind) data[i] <- as.data.frame(strptime(data[[i]], 
							col_dfmts[i]))
	for (i in seq_len(length(data))) if (is.factor(data[[i]])) 
			levels(data[[i]]) <- gsub("\\\\", "", levels(data[[i]]))
	names(data) <- col_names
	col_names = setdiff(col_names, remove)
	data[, col_names]
}




