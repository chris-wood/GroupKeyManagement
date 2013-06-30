%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% File: runSim.m
% Author: Christopher Wood, caw4567@rit.edu
% Description: Monte carlo simulation for the key distribution times
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Simulation parameters
numSamples = 100; %1000 or 10000 for proper results
maxChildren = [4];
nodeCount = [10];%,6,7,8]; 
p1Probs = [0.5];%[1, 0.9, 0.75, 0.5, 0.25, 0.1]; %0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0];
p2Probs= [0.5];%[1, 0.9, 0.75, 0.5, 0.25, 0.1]; %01,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0];
[~, numNodes] = size(nodeCount);
[~, numP1probs] = size(p1Probs);
[~, numP2probs] = size(p2Probs);
[~, numChildren] = size(maxChildren);

% Result containers
times = zeros(numChildren, numP1probs, numP2probs, numNodes, numSamples);
finalTable = zeros(numChildren, numP1probs, numP2probs, numNodes, 4);

% Each epoch will be of size t2, and t1 = 4*t2 (it's about 4 times longer)
kMult = 4; % making this anything different severly impacts the MODEL's performance

% Run the simulation nSamples times
disp('Starting the simulation...');
for childIndex = 1:numChildren
    disp(sprintf('Maximum number of children = %d', maxChildren(childIndex)))
    for p1Index = 1:numP1probs
        for p2Index = 1:numP2probs
            for n = 1:numNodes
                disp(sprintf('Simulation for %d nodes with p1 = %d and p2 = %d', nodeCount(n), p1Probs(p1Index), p2Probs(p2Index)))
                for i = 1:numSamples
                    % Initialize the adj. matrix representation for the nodes and network
                    % No one is connected at the beginning...
                    time = 0; % time = #t2 events
                    nConnected = 0;

                    % The matrix to store authentication steps in time.
                    authMatrix = zeros(kMult, nodeCount(n), nodeCount(n));

                    % The adjacency matrix stores those nodes node connections (the
                    % tree).
                    aMatrix = zeros(nodeCount(n), nodeCount(n));

                    % The connected vector that indicates whether a node has
                    % the key (it is connected).
                    cMatrix = zeros(1, nodeCount(n));

                    % Set the root node to have the key at time 0
                    cMatrix(1) = 1;

                    % Loop while we do try to establish a connection with each node
                    while (nConnected < (nodeCount(n) - 1)) % We go until connected == (n-1)
                        
                        % DEBUG
                        % fprintf('Time step: %i\n', time);

                        % Find the unconnected nodes from the connected list
                        tempList = zeros(1, nodeCount(n));
                        tempListBack = zeros(1, nodeCount(n));
                        nUnconnected = 0;
                        for j = 1:nodeCount(n)
                            tempList(j) = -1; % mark as invalid to start...
                            if (cMatrix(j) == 0)
                                nUnconnected = nUnconnected + 1;
                                tempList(nUnconnected) = j; % Flag as unconnected
                            end
                        end

                        % Strip out all nodes that are currently in 
                        % authentication stage.
                        for kIndex = 1:(kMult) % all nodes in the last stage will be ready for the key...
                           for rIndex = 1:nodeCount(n)
                              for cIndex = 1:nodeCount(n)
                                 if (authMatrix(kIndex, rIndex, cIndex) == 1)
                                    % cIndex is being authenticated by rIndex,
                                    % so take out cIndex from the list if it's
                                    % in there.
                                    for nIndex = 1:nodeCount(n)
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
                            if (rand(1) <= p1Probs(p1Index))
                                readyList(j) = 1; % Flag it as ready for authentication
                                nReady = nReady + 1;
                            end
                        end

                        %%% We need to work backwards through the
                        %%% authentication stages so we don't accidentally
                        %%% carry connections through the pipeline in the same
                        %%% instance in time (only one transition at a time)

                        % Handle the key distribution step now (authentication
                        % is complete at this stage in the auth matrix)
                        for rIndex = 1:nodeCount(n)
                           for cIndex = 1:nodeCount(n)
                              if (authMatrix(kMult, rIndex, cIndex) == 1) % was kMult, not kMult - 1
                                  % A connection exists, use the key
                                  % probability to see if the key
                                  % connection is passed along...
                                  %if (rand(1) <= p2Probs(p2Index))
                                      %disp(sprintf('(%d) The key was passed from node %d to node %d', time, rIndex, cIndex));
                                      authMatrix(kMult, rIndex, cIndex) = 0; % no longer in the authentication stage...
                                      aMatrix(rIndex, cIndex) = 1;
                                      aMatrix(cIndex, rIndex) = 1;
                                      cMatrix(cIndex) = 1;
                                      nConnected = nConnected + 1;

                                      % fprintf('Node %i now has the key\n', cIndex);
                                  %end
                              end
                           end
                        end

                        % Now check to see if the nodes doing
                        % authentication march forwards in time
                        bound = kMult - 1;
                        for kIndex = bound:-1:1
                            for rIndex = 1:nodeCount(n)
                               for cIndex = 1:nodeCount(n)
                                  % If a pair of nodes is attempting
                                  % authentcation, check to see if they
                                  % make progress
                                  if (authMatrix(kIndex, rIndex, cIndex) == 1)
                                      if (rand(1) <= p2Probs(p2Index))
                                          %disp(sprintf('(%d) Authentication between nodes %d and %d advances', time, rIndex, cIndex))
                                          authMatrix(kIndex + 1, rIndex, cIndex) = 1;
                                          authMatrix(kIndex, rIndex, cIndex) = 0;
                                          % fprintf('Node %i advanced to stage %i\n', cIndex, kIndex + 1);
                                      end
                                  end
                               end
                            end
                        end

                        % Compute the set of available parents at this iteration
                        [parentList, parentCount] = readyParents(aMatrix, cMatrix, authMatrix, maxChildren(childIndex), nodeCount(n), kMult);
                        %disp('parents available at this time');
                        %for j = 1:parentCount
                        %   disp(parentList(j));
                        %end

                        % Shuffle algorithm
                        for j = parentCount:-1:1
                            index = randi(j,1);
                            temp = parentList(index);
                            parentList(index) = parentList(j);
                            parentList(j) = temp;
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

                            % DEBUG
                            % fprintf('Node %i is in stage 1\n', unconnected(readyIndex));

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
                    times(childIndex,p1Index,p2Index,n,i) = time;
                end
            end
        end
    end
end

% Generate a plot for each one
figureId = 1;
for childIndex = 1:numChildren
    for p1Index = 1:numP1probs
        temp = zeros(numP2probs, numNodes);
        for p2Index = 1:numP2probs
           for n = 1:numNodes
              %temp(p, n) = avgTimes(c, p, n); % the final table has the correct values
              temp(p2Index, n) = mean(times(childIndex,p1Index,p2Index,n,:)); % the second element is the average time
           end
        end
        %figure(figureId);
        %figureId = figureId + 1; % forward the figures... math is fun.
        %plot(temp);
        %set(gca,'XTickLabel',{'0.1', '0.2', '0.3', '0.4', '0.5', '0.6', '0.7','0.8', '0.9', '1.0'});
        %title([sprintf('Key Distribution Time for %d Children with Key Probability = %d', maxChildren(childIndex), keyProbabilities(pKeyIndex))]);
        %xlabel('Authentication Probability');
        %ylabel('Average Re-Key Time (epochs)');
    end
end

% Calculate the average and standard deviation for each node simulation
% finalTable = zeros(numChildren, numP1probs, numP2probs, numNodes, 4);
for childIndex = 1:numChildren
  for p1Index = 1:numP1probs
    for p2Index = 1:numP2probs
      for i = 1:numNodes
        avg = mean(times(numChildren,p1Index,p2Index, i,:));
        stddev = std(times(numChildren, p1Index, p2Index, i,:));
        stderr = 2 * (stddev / (numSamples^(1/2)));
        fprintf('%d, %d, %d, %d, %d, %d, %d, %d\n', kMult, maxChildren(numChildren), nodeCount(i), p1Probs(p1Index), p2Probs(p2Index), avg, stddev, stderr);
        finalTable(numChildren, p1Index,p2Index,i,1) = nodeCount(i);
        finalTable(numChildren, p1Index,p2Index,i,2) = avg;
        finalTable(numChildren, p1Index,p2Index,i,3) = stddev;
        finalTable(numChildren, p1Index,p2Index,i,4) = stderr;
      end
    end
  end
end

% Display the final table
%disp(finalTable);
