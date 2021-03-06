function ms_edges = findEdges(source_file, start, dur, num_words, demo)
% FINDEDGES find the edges of the words in a given audio clip
%   source_file - Input Audio File
%   start - Start time from the caption line
%   dur - Duration of the caption line
%   num_words - number of words in the caption line (number of peaks we are
%               looking for)
%
%   Returns vector of edge times relative to the given start time in ms.

if ~exist('demo','var')
    demo = false;
end

[y, Fs] = audioread(source_file);

% ==== Edge Detection Config ====
MIN_PEAK_HEIGHT_INIT = 0.100;
MIN_PEAK_HEIGHT_STEP = 0.005;
MIN_PEAK_DISTANCE = Fs * 0.15;
WINDOW_SIZE = ceil(Fs * 0.01);
TEXT_OFFSET = -3;
% ==== End Config ====

sample = y(floor(Fs * (start + TEXT_OFFSET)): ceil(Fs * (start + dur + TEXT_OFFSET)));
x = 1:length(sample);

% Begin Detection

close all

dy = gradient(sample, mean(diff(x))); % First Dertivitive
dypks = [];

warning('off','signal:findpeaks:largeMinPeakHeight')  % Supress "Invalid MinPeakHeight" Warnings
while length(dypks) < num_words
    %     Find the peaks of the words. We start with a relatively high peak,
    %     and decrease the minimum peak height repeatedly until we find the
    %     number of peaks we are looking for.
    [dypks,ix] = findpeaks(dy, 'MinPeakDistance',MIN_PEAK_DISTANCE, 'MinPeakHeight',MIN_PEAK_HEIGHT_INIT);
    MIN_PEAK_HEIGHT_INIT = MIN_PEAK_HEIGHT_INIT - MIN_PEAK_HEIGHT_STEP;
end

ranges = [0 ix max(x)];

% ddy = gradient(dy, mean(diff(x)));  % Second Dertivitive
y_b =  downsample(dy, WINDOW_SIZE);
bucket_x = (1:length(y_b)) .* WINDOW_SIZE;

edges = [0]; % First Edge

%  /*\/*$\/$\
warning('off', 'MATLAB:polyfit:RepeatedPointsOrRescale');
for p = 2:length(dypks)   % Skip first and last edge, since they are known
    l_edge = ranges(p);
    r_edge = ranges(p+1);
    
    % There are in theory 2 "edges," hence the fourth-degree polyfit
    curve = polyfit(l_edge:r_edge,sample(l_edge:r_edge),4);
    d1a = polyder(curve);              % First derivative of Best Fit Curve
    ip = roots(d1a);  
    edges = [edges median(ip) median(ip)]; % Middle Edges
end

edges = [edges max(x)]; % Last Edge

if demo
    figure(1)
    plot(x, sample)
    hold on
    plot(x, dy)
    plot(bucket_x, y_b)
    plot(edges', 0, '^b', 'MarkerFaceColor','b')
    plot(x(ix), dypks*1E-9, '^g', 'MarkerFaceColor','g')
    hold off
    grid
end

ms_edges = edges ./ Fs;

end

