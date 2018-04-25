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
sample = y(floor(Fs * start): ceil(Fs * (start + dur)));
x = 1:length(sample);

% ==== Edge Detection Config ====
MIN_PEAK_HEIGHT_INIT = 0.100;
MIN_PEAK_HEIGHT_STEP = 0.005;
MIN_PEAK_DISTANCE = Fs * 0.15;
WINDOW_SIZE = ceil(Fs * 0.01);
EDGE_EPSILON = -1e-4;
% ==== End Config ====

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

ddy = gradient(dy, mean(diff(x)));  % Second Dertivitive
ddy_b =  downsample(dy, WINDOW_SIZE);
bucket_x = (1:length(ddy_b)) .* WINDOW_SIZE;

edges = zeros(length(dypks), 2);

for p = 1:length(dypks)
    peak_x = ix(p);
    
    %    Find Left Edge
%     i = 1;
%     while true
%         if dy_b(floor(peak_x / WINDOW_SIZE) - i) < EDGE_EPSILON
%             edges(p, 2) = peak_x - i * WINDOW_SIZE;
%             break
%         end
%         
%         i = i + 1;
%     end     

    %    Find Right Edge
    i = 1;
    while true
        if ddy_b(floor(peak_x / WINDOW_SIZE) + i) < EDGE_EPSILON
            edges(p, 2) = peak_x + i * WINDOW_SIZE;
            break
        end
        
        i = i + 1;
    end     
end


if demo
    figure(1)
    plot(x, sample)
    hold on
    plot(x, dy)
    plot(bucket_x, ddy_b)
%     plot(x(edges(:,1)'), 0, '^b', 'MarkerFaceColor','b')
    plot(edges(:,2)', 0, '^b', 'MarkerFaceColor','b')
    plot(x(ix), dypks*1E-9, '^g', 'MarkerFaceColor','g')
    hold off
    grid
end

ms_edges = edges ./ Fs;

end

