%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% File: script.m
% Author: Christopher Wood, caw4567@rit.edu
% Description: Script that uses pre-computed values to run the E(Td) function
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Fixed input into the time function
N = [5, 10 15]
P = [0.5,0.75,0.9,0.95,1.0]

% Resulting time computations
T = zeros(size(P)(2), size(N)(2));

% Compute all time combinations and then display the result
for i = 1:size(P)(2)
	for j = 1:size(N)(2)
		%val = time(0, 0, N(i), P(j));
		T(i,j) = time(0, 0, P(i), N(j));
	end
end

% Display the result (row by row)
T

% Plot the data
%plot(T)
%title ("Relative Packet Distribution Times");
%xlabel ("N (number of nodes)");
%ylabel ("Estimated Time (Td)");

% Now calculate the distribution time 
DT = zeros(size(P)(2), size(N)(2));
for i = 1:size(P)(2)
	for j = 1:size(N)(2)
		%DT(i,j) = disttime(N(j), P(i), 85000, 1072, 7);
	end
end

% Display distribution time
%DT

% Plot the data
%plot(DT)
%title("Actual Packet Distribution Times");
%xlabel("N (number of nodes)");
%ylabel("Estimated Time (Td)");
 
