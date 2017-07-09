###

library(MASS)
fileIn <- "T1D_size1_best100_Training.rs.csv"
trainSet <- read.table(fileIn, sep=',', header=TRUE)

newCol <- trainSet$phenotype-1 # transform {1,2} to {0,1}
trainSet$phenotype <- newCol

### backward stepwise
train.glm <- glm(phenotype ~ ., family = binomial, data = trainSet) #full model
#train.step <- stepAIC(train.glm, trace = FALSE)
#train.step$anova

### forward stepwise
train.glm2 <- glm(phenotype ~ 1, family = binomial, data = trainSet) #empty model
train.step <- stepAIC(train.glm2, scope=list(lower=formula(train.glm2),upper=formula(train.glm)), direction="forward", trace = FALSE)
train.step$anova
