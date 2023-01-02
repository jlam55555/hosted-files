clc; close all; clear;
load('projIA.mat');

%% create direct form 1 filter
Hd = dfilt.df1(b, a);
plot_filter(Hd, 100, 'N=1');

%% save audio to file
audiowrite('n1.wav', filter(b, a, speech), fs);

%% attempted convolution
b50 = b;
a50 = a;
for i = 1:49
    b50 = conv(b, b50);
    a50 = conv(a, a50);
end % doesn't work; bad numerical stability

% used for second-order sections filters
[s, g] = tf2sos(b, a);

%% direct form 1
Hd_df1 = cascade_filter(dfilt.df1(b, a));
plot_filter(Hd_df1, 5000, 'N=50, DF1');
audiowrite('df1.wav', filter(Hd_df1, speech), fs);

%% direct form 1 (second-order sections)
Hd_df1sos = cascade_filter(dfilt.df1sos(s, g));
plot_filter(Hd_df1sos, 5000, 'N=50, DF1SOS');
audiowrite('df1sos.wav', filter(Hd_df1sos, speech), fs);

%% direct form 2 (second-order sections)
Hd_df2sos = cascade_filter(dfilt.df2sos(s, g));
plot_filter(Hd_df2sos, 5000, 'N=50, DF2SOS');
audiowrite('df2sos.wav', filter(Hd_df2sos, speech), fs);

%% direct form 2 transposed (second-order sections)
Hd_df2tsos = cascade_filter(dfilt.df2tsos(s, g));
plot_filter(Hd_df2tsos, 5000, 'N=50, DF2TSOS');
audiowrite('df2tsos.wav', filter(Hd_df2tsos, speech), fs);

%% cascade filter 50 times
function Hd = cascade_filter(Hd)
    Hd = dfilt.cascade(repmat(Hd, 1, 50));
end

%% plot impulse response, frequency response, group delay, and
%  pole-zero plot of filter
function plot_filter(Hd, N, figname)
    impz(Hd, N);
    title(sprintf('Impulse response; %s', figname));
    freqz(Hd);
    title(sprintf('Frequency response; %s', figname));
    grpdelay(Hd);
    title(sprintf('Group delay; %s', figname));
    zplane(Hd);
    title(sprintf('Pole-zero plot; %s', figname));
end