
% find user average
nusers = 46000;
total = zeros(nusers, 1);
count = zeros(nusers, 1);
user_avg = nan(nusers, 1);
% 
% for i = 1:nusers
%     i
%     user_avg(i) = mean(data(data(:, 1) == i, 3));
% end

disp 'calculating the average...'
for i = 1:length(data)
    current = data(i, :);
    total(current(1)) = total(current(1)) + current(3);
    count(current(1)) = count(current(1)) + 1;
end

for i = 1:nusers
    if(count(i) ~= 0)
        user_avg(i) = total(i)/count(i);
    end
end

global_user_avg =  mean(user_avg(~isnan(user_avg)));
user_avg(isnan(user_avg)) = global_user_avg;


disp 'making a prediction...'
prediction = user_avg(testGraph(:, 1));
RMSE(testGraph(:, 3), prediction)