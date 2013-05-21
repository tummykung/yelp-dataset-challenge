function output = predict(M_U, M_V, i, j)
output =  M_U(i, :) * M_V(j, :)';
end