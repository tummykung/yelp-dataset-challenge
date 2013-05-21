function output = RMSE(test, prediction)

n = length(prediction);
output = 1/sqrt(n) * sqrt(sum((test-prediction).^2));

end