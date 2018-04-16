function findEdges(source_file, start, dur, num_words)
% FINDEDGES find the edges of the words in a given audio clip
%   source_file - Input Audio File
%   start - Start time from the caption line
%   dur - Duration of the caption line
%   num_words - number of words in the caption line (number of peaks we are
%               looking for)

[y, Fs] = audioread(source_file);
sample = y(floor(Fs * start): ceil(Fs * (start + dur)));
x = 1:length(sample);

% ==== Edge Detection Config ====
MIN_PEAK_HEIGHT_INIT = 0.100;
MIN_PEAK_HEIGHT_STEP = 0.005;
MIN_PEAK_DISTANCE = Fs * 0.15;
% ==== End Config ====

% Begin Detection

close all

dy = gradient(sample, mean(diff(x)));
dypks = [];

warning('off','signal:findpeaks:largeMinPeakHeight')  % Supress "Invalid MinPeakHeight" Warnings
while length(dypks) < num_words
    %     Find the peaks of the words. We start with a relatively high peak,
    %     and decrease the minimum peak height repeatedly until we find the
    %     number of peaks we are looking for.
    [dypks,ix] = findpeaks(dy, 'MinPeakDistance',MIN_PEAK_DISTANCE, 'MinPeakHeight',MIN_PEAK_HEIGHT_INIT);
    MIN_PEAK_HEIGHT_INIT = MIN_PEAK_HEIGHT_INIT - MIN_PEAK_HEIGHT_STEP;
end

figure(1)
plot(x, sample)
hold on
plot(x, dy)
plot(x(ix), dypks*1E-9, '^g', 'MarkerFaceColor','g')
hold off
grid
axis([0  1E-7    ylim])

end

