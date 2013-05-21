#todo: chars 3mal, einmal normal, einmal nach na spalten raus, einaml nach na zeilen raus

data.chars <- function(data, name, target=colnames(data)[ncol(data)] ) { 
    matrix1 <- data.frame(matrix(0,ncol=22,nrow=1))
    colnames(matrix1) <- c("ds", "n.obs", "n.classes", "n.minclass", "n.maxclass", "q.maxminclass", 
            "n.inputs", "n.nums","q.nums","n.ints","q.ints","n.fac","q.fac", "n.char","q.char", 
            "n.narows","maxnarow","q.maxnarow","n.nacols","maxnacol","q.maxnacol", "n.nas")
    NAs <- sum(is.na(data))
    rows.with.missings <- sum(apply(data, 1, function(x) any(is.na(x))))
    cols.with.missings <- sum(apply(data, 2, function(x) any(is.na(x))))
    numerics <- sum(sapply(data, is.numeric))
    integers <- sum(sapply(data, is.integer))
    factors <- sum(sapply(data, is.factor))
    characters <- sum(sapply(data, is.character))
    class <- length(unique(as.numeric(data[,target])))
    min.class <- min(table(data[,target]))
    max.class <- max(table(data[,target]))
    maxmin.class <- max.class/min.class
    obs <- length(data[,target])
    input <- length(data)
    numinput <- numerics/input
    intinput <- integers/input
    facinput <- factors/input
    charinput <- characters/input
    maxnarow <- max(apply(data, 1, function(x) sum(is.na(x))))
    q.maxnarow <- maxnarow/(ncol(data)-1)
    maxnacol <- max(apply(data, 2, function(x) sum(is.na(x))))
    q.maxnacol <- maxnacol/nrow(data)
    
    
    
    matrix1[1,2:ncol(matrix1)] <- c(obs,class,min.class,max.class,maxmin.class,input,numerics,numinput,
            integers,intinput,factors,facinput,characters,charinput,
            rows.with.missings,maxnarow,q.maxnarow,cols.with.missings,maxnacol,q.maxnacol,NAs)
    matrix1[1, 1] <- name
    return(matrix1)
}
