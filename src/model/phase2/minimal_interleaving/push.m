function [ ds ] = pushLast( k, m, n, D ) % D = dMax with the first invocation
% Dmax is a (k x m) matrix

disp('Entering push')
disp(D)
disp(k)
disp(m)
disp(n)

% Start at the last column in the D matrix and push down, bearing constraints in mind
for j = m:-1:1 
	lastRow = D(1,j);
	for i = 2:k % We have at least two children, so this won't fail.
		if (D(i,j) <= lastRow)
			lastRow = D(i,j);
		else
			disp('CONSTRAINTS VIOLATED - PUT ON BUG SEARCH GOGGLES (it is too late :-\)')
		end
	end

	i = 1
	D(i,j) = D(i,j) - 1;
	if (j == m)
		% Push down to the next row (we're at the last stage of communication)
		D(i + 1, 1) = D(i + 1, 1) + 1;
	else
		% Move the node forward in the D space...
		D(i,j + 1) = D(i, j + 1) + 1;
	end	

	disp('New Matrix.')
	disp(D)
end
