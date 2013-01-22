%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% File: runSim.m
% Author: Christopher Wood, caw4567@rit.edu
% Description: Monte carlo simulation for the key distribution times
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Simulation parameters
numSamples = 1000; %1000 or 10000 for proper results 
maxChildren = 2;
numNodes = [5,10,15,20,25,30]; % return after the thing is working!
authProbabilities = [0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0];
keyProbabilities = [0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0];
[~, numSims] = size(numNodes);
[~, numAuthProbs] = size(authProbabilities);
[~, numKeyProbs] = size(keyProbabilities);

% Result containers
times = zeros(numAuthProbs, numKeyProbs, numSims,numSamples); 
avgTimes = zeros(numAuthProbs, numKeyProbs, numSims);
finalTable = zeros(numAuthProbs, numKeyProbs, numSims, 4);

% Each epoch will be of size t2, and t1 = 4*t2 (it's about 4 times longer)
kMult = 4;

% Run the simulation nSamples times
disp('Starting the simulation...');
totalTime = 0;
for pAuthIndex = 1:numAuthProbs
    for pKeyIndex = 1:numKeyProbs
        for n = 1:numSims
            disp(sprintf('Simulation for %d nodes with pAuth = %d and pKey = %d', numNodes(n), authProbabilities(pAuthIndex), keyProbabilities(pKeyIndex)))
            for i = 1:numSamples
                % Initialize the adj. matrix representation for the nodes and network
                % No one is connected at the beginning...
                time = 0; % time = #t2 events
                nConnected = 0;

                % The matrix to store authentication steps in time.
                authMatrix = zeros(kMult, numNodes(n), numNodes(n));

                % The adjacency matrix stores those nodes node connections (the
                % tree).
                aMatrix = zeros(numNodes(n), numNodes(n));

                % The connected vector that indicates whether a node has
                % the key (it is connected).
                cMatrix = zeros(1, numNodes(n));

                % Set the root node to have the key at time 0
                cMatrix(1) = 1;

                % Loop while we do try to establish a connection with each node
                while (nConnected < (numNodes(n) - 1)) % We go until connected == (n-1)
                    % Find the unconnected nodes from the connected list
                    tempList = zeros(1, numNodes(n));
                    tempListBack = zeros(1, numNodes(n));
                    nUnconnected = 0;
                    for j = 1:numNodes(n)
                        tempList(j) = -1; % mark as invalid to start...
                        if (cMatrix(j) == 0)
                            nUnconnected = nUnconnected + 1;
                            tempList(nUnconnected) = j; % Flag as unconnected
                            %tempListBack(j) = nUnconnected; % Mark back pointer
                            %disp('adding because not in connected vector')
                            %disp(j)
                        end
                    end

                    % Strip out all nodes that are currently in 
                    % authentication stage.
                    for kIndex = 1:kMult
                       for rIndex = 1:numNodes(n)
                          for cIndex = 1:numNodes(n)
                             if (authMatrix(kIndex, rIndex, cIndex) == 1)
                                % cIndex is being authenticated by rIndex,
                                % so take out cIndex from the list if it's
                                % in there.
                                for nIndex = 1:numNodes(n)
                                    if (tempList(nIndex) == cIndex)
                                        tempList(nIndex) = -1; % set it back to invalid
                                        nUnconnected = nUnconnected - 1; % decrement since we took it out of the list
                                    end
                                end
                             end
                          end
                       end
                    end

                    % Build up the unconnected list
                    unconnected = zeros(1, nUnconnected);
                    tempIndex = 1;
                    for j = 1:nUnconnected
                        % Skip over invalid entries
                        while (tempList(tempIndex) == -1)
                            tempIndex = tempIndex + 1;
                        end
                        
                        % Add this element to the list
                        unconnected(j) = tempList(tempIndex);
                        tempIndex = tempIndex + 1;
                    end

                    % For each node that is ready, decide with probability p
                    % if it should receive the key at this instance in time.
                    readyList = zeros(1, nUnconnected);
                    nReady = 0;
                    for j = 1:nUnconnected
                        if (rand(1) <= authProbabilities(pAuthIndex))
                            readyList(j) = 1; % Flag it as ready for authentication
                            nReady = nReady + 1;
                        end
                    end
                    
                    %%% We need to work backwards through the
                    %%% authentication stages so we don't accidentally
                    %%% carry connections through the pipeline in the same
                    %%% instance in time (only one transition at a time)
                    
                    % Compute the set of available parents at this iteration
                    [parentList, parentCount] = readyParents(aMatrix, cMatrix, authMatrix, maxChildren, numNodes(n), kMult);

                    % Shuffle algorithm
                    for j = parentCount:-1:1
                        index = randi(j,1);
                        temp = parentList(index);
                        parentList(index) = parentList(j);
                        parentList(j) = temp;
                    end
                    
                    % Handle the key distribution step now (authentication
                    % is complete at this stage in the auth matrix)
                    for rIndex = 1:numNodes(n)
                       for cIndex = 1:numNodes(n)
                          if (authMatrix(kMult, rIndex, cIndex) == 1)
                              % A connection exists, use the key
                              % probability to see if the key
                              % connection is passed along...
                              if (rand(1) <= authProbabilities(pKeyIndex))
                                  %disp(sprintf('(%d) The key was passed from node %d to node %d', time, rIndex, cIndex));
                                  authMatrix(kMult, rIndex, cIndex) = 0; % no longer in the authentication stage...
                                  aMatrix(rIndex, cIndex) = 1;
                                  aMatrix(cIndex, rIndex) = 1;
                                  cMatrix(cIndex) = 1;
                                  nConnected = nConnected + 1;
                              end
                          end
                       end
                    end

                    % Now check to see if the nodes doing
                    % authentication march forwards in time
                    bound = kMult - 1;
                    for kIndex = bound:-1:1
                        for rIndex = 1:numNodes(n)
                           for cIndex = 1:numNodes(n)
                              % If a pair of nodes is attempting
                              % authentcation, check to see if they
                              % make progress
                              if (authMatrix(kIndex, rIndex, cIndex) == 1)
                                  if (rand(1) <= authProbabilities(pAuthIndex))
                                      %disp(sprintf('(%d) Authentication between nodes %d and %d advances', time, rIndex, cIndex))
                                      authMatrix(kIndex + 1, rIndex, cIndex) = 1;
                                      authMatrix(kIndex, rIndex, cIndex) = 0;
                                  end
                              end
                           end
                        end
                    end

                    % Find upper bound on connections
                    bound = min(parentCount, nReady);

                    % Start these nodes off in the authentication step
                    readyIndex = 1;
                    for j = 1:bound
                        % Skip over nodes that were deemed not ready
                        while (readyList(readyIndex) == 0)
                            readyIndex = readyIndex + 1;
                        end

                        % Hook these guys into the auth matrix
                        child = unconnected(readyIndex);
                        parent = parentList(j);
                        %disp(sprintf('(%d) Node %d starting authentication with node %d', time, parent, child))
                        authMatrix(1, parent, child) = 1; % this is a directed graph, so don't point from child->parent
                        readyIndex = readyIndex + 1;
                    end

                    time = time + 1;
                end
                
                % Take away the last step in time (handle off-by-one)
                time = time - 1;
                %disp(sprintf('Total time: %d', time))
                
                % Record the total time for simulation
                totalTime = totalTime + time;
                times(pAuthIndex,pKeyIndex,n,i) = time;
            end

            % Save the average time thus far (redundant...)
            avgTimes(pAuthIndex,pKeyIndex,n) = totalTime / numSamples;
        end
    end
end

% Generate a plot for each one
%for c = 1:numChildren
for pKeyIndex = 1:numKeyProbs
    temp = zeros(numAuthProbs, numSims);
    for p = 1:numAuthProbs
       for n = 1:numSims
          %temp(p, n) = avgTimes(c, p, n); % the final table has the correct values
          temp(p, n) = mean(times(p,pKeyIndex,n,:)); % the second element is the average time
       end
    end
    figure(pKeyIndex)
    plot(temp);
    set(gca,'XTickLabel',{'0.1', '0.2', '0.3', '0.4', '0.5', '0.6', '0.7','0.8', '0.9', '1.0'});
    title([sprintf('Key Distribution Time for 2 Children with Key Probability = %d', keyProbabilities(pKeyIndex))]);
    xlabel('Authentication Probability');
    ylabel('Average Re-Key Time (epochs)');
end
%end

% Calculate the average and standard deviation for each node simulation
%for pAuthIndex = 1:numAuthProbs
%    for pKeyIndex = 1:numKeyProbs
%        for i = 1:numSims
%            avg = mean(times(pAuthIndex, pKeyIndex, i,:));
%            stddev = std(times(pAuthIndex, pKeyIndex, i,:));
%            stderr = 2 * (stddev / (numSamples^(1/2)));
%            finalTable(pAuthIndex,pKeyIndex,i,1) = numNodes(i);
%            finalTable(pAuthIndex,pKeyIndex,i,2) = avg;
%            finalTable(pAuthIndex,pKeyIndex,i,3) = stddev;
%            finalTable(pAuthIndex,pKeyIndex,i,4) = stderr;
%        end
%    end
%end

% Display the average times table
%disp(avgTimes);

% Display the final table
%disp(finalTable);
