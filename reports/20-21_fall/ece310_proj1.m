% Jonathan Lam
% Prof. Keene
% ECE310
% Digital Signal Processing
% 9 / 29 / 20

clear; close all; clc;

% testing wagner; resample and write to new file
[x, fs] = audioread('Wagner.wav');
x = x.';
[y, flo] = srconvert(x);
fprintf('FLOs per sample: %d\n', flo);
audiowrite('resampledWagner.wav', y, 24000);

% plot x and y
subplot(2, 1, 1);
t = linspace(0, length(x)/fs, length(x));
plot(t, x);
title(sprintf('Original, sampled at 11025Hz (%d samples)', length(x)));
xlabel('t (s)');
ylim([-1 1]);
xlim([0 length(x)/fs]);
subplot(2, 1, 2);
t = linspace(0, length(x)/fs, length(y));
plot(t, y);
title(sprintf('Sample rate-converted to 24000Hz (%d samples)', length(y)));
xlabel('t (s)');
ylim([-1 1]);
xlim([0 length(x)/fs]);
figure();

% verify that it works
ir = srconvert([1 zeros(1,3000)]);
verify(ir);

% efficiently converts sampling rate from 11.025kHz to 24kHz
% (or any other resampling with a ratio of 320/147)
% returns the samplerate-converted signal and the number of FLOs per sample
function [x, flo] = srconvert(x)
    % break down into three stages
    [x, flo1] = efficientresample(x, 5, 3);
    [x, flo2] = efficientresample(x, 8, 7);
    [x, flo3] = efficientresample(x, 8, 7);
    
    % number of FLOs per sample
    flo = flo1 + flo2 + flo3;
end

% uses polyphase decomposition to efficiently up- and down-sample x
function [res, flo] = efficientresample(x, L, M)
    % design the LPF
    % source: https://www.mathworks.com/help/signal/ref/cheb2ord.html
    Wp = min(1/L, 1/M);
    Ws = Wp * 1.2;
    Rp = 0.1;
    Rs = 85;
    [n, Ws] = cheb2ord(Wp, Ws, Rp, Rs);
    [b, a] = cheby2(n, Rs, Ws, 'low');
    h = impz(b, a).';

    % decompose h into its polyphase components
    hc = poly1(h, L);
    
    % do all the convolutions
    res = zeros(1, L * length(x));
    for i = 1:L
        component = upsample(fftfilt(hc(i, :), x), L);
        res = res + [zeros(1, i-1) component(1:length(component)+1-i)];
    end
    
    % decimation and rescaling
    res = L * downsample(res, M);
    
    % counting calculations; same calculation as in textbook;
    % L * (N/L) = N multiplications/(sample period) in convolutions
    % L * (N/L - 1) = N - L adds/(sample period) in convolutions
    % L - 1 additions when adding polyphase components together
    % total = N + (N - L) + (L - 1) = 2 * N + 1 FLO per sample
    flo = 2*length(h) + 1;
end