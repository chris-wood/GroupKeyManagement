%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% File: modelScript.m
% Author: Christopher Wood, caw4567@rit.edu
% Description: Script that uses pre-computed values to run 
%	the E(Td) function
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Fixed input into the time function
N = [5, 10, 15, 20, 25, 30];
P = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0];
[nr, nc] = size(N);
[pr, pc] = size(P);

% Display the test data
disp(N);
disp(P);

% Resulting time computations
T = zeros(pc, nc);

k = 3;
m = 2;
n = 5;

dSpaces = zeros(k,m,n);

% D^n max configuration
for j = 1:m
	dSpaces(1,j,5) = n - 1;
end

% I wanna push you around, well I will
% I wanna push you down, well I will
% I wanna take you for granted
% I wanna take you for granted, and I will
% Credit: Matchbox Twenty - Push
% ... Now the push function :-)
ds = {}
ds = pushLast(1, k, m, n, dSpaces(:,:,5), ds);

disp('Resulting matrix...');

% TODO: push down to the other D subspaces to get to D^0... using probabilities OC

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% nUnknown;  % The number of unknown arrays
% myArray = zeros(500,800,nUnknown);
% subMatrix = myArray(:,:,3);  % Gets the third matrix

% Add...
% myArray = cat(3,myArray,zeros(500,800));
% myArray(:,:,nUnknown+1) = zeros(500,800);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Compute all time combinations and then display the result
% for i = 1:pc
% 	for j = 1:nc
% 		% Resulting time computations
% 		ET = zeros(N(1,j) + 1, N(1,j) + 1);

% 		% Fill in the base case for the expected time (along the diagonal)
% 		for d2 = 1:N(1,j) + 1
% 			for d3 = 1:N(1,j) + 1
% 				if (d2 + d3 == N(1,j) + 1)
% 					d2;
% 					d3;
% 					ET(d2, d3) = 0;
% 				end
% 			end
%         end
        
%         % Display the current progress
%         disp(T);

% 		% Walk across the diagonals using the fact that (d2+d3) = (n-1)
% 		for height = 0:(N(1,j) - 2)
% 			for d2 = 1:(N(1,j) - 1)
% 				d3 = N(1,j) - d2 - height;
                
%                 % Champion.
% 				if (d3 >= d2 && d2 + d3 <= N(1,j))
% 					ET(d2, d3) = time(d2 - 1, d3 - 1, P(i), N(1,j), ET);
% 				end
% 			end
% 		end

% 		% Display the answer
% 		T(i,j) = ET(1,1); % ET(1,1) = ET(0,0) in actual model
% 	end
% end

%plot(T)
% NewT = T';
% plot(N, NewT)

% Plot values for T with fixed node numbers and varied probability
% for i = 1:nc
%     temp = zeros(1, pc);
%     for j = 1:pc
%         temp(1,j) = T(j,i);
%     end
%     %plot(temp)
% end
