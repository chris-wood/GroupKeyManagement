function [ ds ] = pushLast( depth, k, m, n, D, ds ) % D = dMax with the first invocation
% Dmax is a (k x m) matrix

% disp('Entering push')
% disp(D)
% disp(k)
% disp(m)
% disp(n)

oldD = D;

% Start at the last column in the D matrix and push down, bearing constraints in mind
j = m; % Max... we only care about the last row...
if (D(1,j) <= 0)
	disp('REACHED ZERO VALUE FOR D SPACE');
	return; % early return
end
lastRow = D(1,j);
for i = 2:k % We have at least two children, so this won't fail.
	if (D(i,j)  <= lastRow)
		lastRow = D(i,j);
	else
		disp('CONSTRAINTS VIOLATED - PUT ON BUG SEARCH GOGGLES (it is too late :-\)')
		return; % early return
	end
end

i = 1;
D(i,j) = D(i,j) - 1;
D(i + 1, j) = D(i + 1, j) + 1;
%if (j == m)
% Push down to the next row (we're at the last stage of communication)
%D(i + 1, 1) = D(i + 1, 1) + 1;
%else
% Move the node forward in the D space...
%	D(i,j + 1) = D(i, j + 1) + 1;
%end

lastRow = D(1,j);
for i = 2:k % We have at least two children, so this won't fail.
	if (D(i,j) <= lastRow)
		lastRow = D(i,j);
	else
		disp('CONSTRAINTS VIOLATED - PUT ON BUG SEARCH GOGGLES (it is too late :-\)')
		return; % early return
	end
end

if (depth == 1)
	disp('Matrix');
	disp(oldD);
	ds = [ds; {oldD}];
end
disp('New/Old Matrix')
disp(D)
ds = [ds; {D}];

% Recurse...
ds = pushLast(depth+1,k,m,n,D,ds);




