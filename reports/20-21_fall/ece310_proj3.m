% Jonathan Lam
% Prof. Keene
% ECE 310
% Proj. 2
% 11 / 4 / 20

%% clear all
clc; clear; close all;

%% load sound
load('projIB.mat');

figure();
plot(noisy);

% play sound (sounds bad)
% soundsc(noisy, fs);

%% filter specs
Wp = 2500 / (fs/2);
Ws = 4000 / (fs/2);
Rp = 3;
Rs = 95;

%% butterworth
[n, Wn] = buttord(Wp, Ws, Rp, Rs);
[z, p, k] = butter(n, Wn);
k = k * 100;

butter_sos = plot_filter_zpk(z, p, k, 'butter');

%% butter df2t sos cascaded version
butter_filter = dfilt.df2tsos(butter_sos);
butter_filtered = filter(butter_filter, noisy);
soundsc(butter_filtered, fs);

%% cheby1
[n, cheby1_Wp] = cheb1ord(Wp, Ws, Rp, Rs);
[z, p, k] = cheby1(n, Rp, cheby1_Wp);
k = k * 100;

cheby1_sos = plot_filter_zpk(z, p, k, 'cheby1');

%% cheby1 df2t sps cascaded version
cheby1_filter = dfilt.df2sos(cheby1_sos);
cheby1_filtered = filter(cheby1_filter, noisy);
soundsc(cheby1_filtered, fs);

%% cheby2
[n, cheby2_Ws] = cheb2ord(Wp, Ws, Rp, Rs);
[z, p, k] = cheby2(n, Rs, cheby2_Ws);
k = k * 100;

cheby2_sos = plot_filter_zpk(z, p, k, 'cheby2');

%% cheby2 df2t sps cascaded version
cheby2_filter = dfilt.df2sos(cheby2_sos);
cheby2_filtered = filter(cheby2_filter, noisy);
soundsc(cheby2_filtered, fs);

%% elliptic
[n, ellip_Wp] = ellipord(Wp, Ws, Rp, Rs);
[z, p, k] = ellip(n, Rp, Rs, ellip_Wp);
k = k * 100;

ellip_sos = plot_filter_zpk(z, p, k, 'ellip');

%% elliptic df2t sps cascaded version
ellip_filter = dfilt.df2sos(ellip_sos);
ellip_filtered = filter(ellip_filter, noisy);
soundsc(ellip_filtered, fs);

%% redesign specs for FIR

% ripple in linear units for the IIR spec
% for passband, it's the difference between 1 and the linear value of the
% attentuation factor
Rp_linear_hat = 1-10^(-Rp/20);
% for stopband, it's the linear value of the attenuation factor
Rs_linear_hat = 10^(-Rs/20);

% using the equations from exercis 7.3 in the textbook to convert to the
% FIR spec (these are all in linear units):
% Rp = Rp_hat/(2-Rp_hat)
% Rs = 2Rs_hat/(2-Rp_hat)
Rp_linear = Rp_linear_hat/(2-Rp_linear_hat);
Rs_linear = 2*Rs_linear_hat/(2-Rp_linear_hat);

f = [Wp Ws];
a = [1 0];

%% parks mclellan
% +6 in filter is a fudge factor; documentation says if it doesn't
% meet the spec, try increasing the order (similar to kaiser windows)
% https://www.mathworks.com/help/signal/ref/firpm.html
[n, fo, ao, w] = firpmord(f, a, [Rp_linear Rs_linear]);
pm_filter = firpm(n+6, fo, ao, w);
H = freqz(pm_filter);
pm_filter = pm_filter / max(abs(H)) * 100;

plot_h(pm_filter, 'pm');

%% parks mclellan convolution
pm_filtered = conv(pm_filter, noisy);
soundsc(pm_filtered, fs);

%% kaiser
[n, Wn, beta, ftype] = kaiserord(f, a, [Rp_linear Rs_linear]);
kaiser_filter = fir1(n, Wn, ftype, kaiser(n+1, beta), 'noscale');
H = freqz(kaiser_filter);
kaiser_filter = kaiser_filter / max(abs(H)) * 100;

plot_h(kaiser_filter, 'kaiser');

%% convolution
kaiser_filtered = conv(kaiser_filter, noisy);
soundsc(kaiser_filtered, fs);

% plot filter given its zpk
function sos = plot_filter_zpk(z, p, k, name)
    sos = zp2sos(z, p, k);
    
    fig = figure('Visible', 'Off');
    tiledlayout(2, 2, 'TileSpacing', 'compact');
    
    nexttile();
    [h, t] = impz(sos, 100);
    stem(t, h);
    title('Impulse Response');
    ylabel('Amplitude');
    xlabel('n (samples)');
    
    nexttile();
    [H, w] = freqz(sos);
    plot(w, 20*log10(abs(H)));
    title('Frequency Respose');
    ylabel('Magnitude (dB)');
    xlabel('Digital Frequency');
    ylim([-100 40]);
    xlim([0 pi]);
    
    nexttile();
    zplane(z, p, k);
    title('Pole-Zero Plot');
    
    nexttile();
    grpdelay(sos);
    title('Group Delay');
    
    set(fig, 'PaperUnits', 'centimeters');
    set(fig, 'PaperPosition', [0 0 30 20]);
    saveas(fig, sprintf('fig_%s.eps', name));
end

% plot filter given its impulse response
function plot_h(h, name)
    fig = figure('Visible', 'Off');
    tiledlayout(2, 2, 'TileSpacing', 'compact');

    nexttile();
    stem(h);
    title('Impulse Response');
    ylabel('Amplitude');
    xlabel('n (samples)');

    nexttile();
    [H, w] = freqz(h);
    plot(w, 20*log10(abs(H)));
    title('Frequency Respose');
    ylabel('Magnitude (dB)');
    xlabel('Digital Frequency');
    ylim([-100 40]);
    xlim([0 pi]);

    nexttile();
    zplane(h);
    title('Pole-Zero Plot');

    nexttile();
    grpdelay(h);
    title('Group Delay');
    
    set(fig, 'PaperUnits', 'centimeters');
    set(fig, 'PaperPosition', [0 0 30 20]);
    saveas(fig, sprintf('fig_%s.eps', name));
end