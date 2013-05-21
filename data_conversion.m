data2 = data;
data2(:, 3) = data(:, 3) - user_avg(data(:, 1));
%%
validation2 = validation;
validation2(:, 3) = validation(:, 3) - user_avg(validation(:, 1));