% PSET1
% Jonathan Lam
% Prof. Frost
% ECE300
% Communications Theory
% 9/7/20

clear; close all; clc;
set(0, 'defaultTextInterpreter', 'latex');

% Q5

% sample details
N = 2000;
T = 10;
fs = N/T;

% sinusoid details
f0 = 2;
T0 = 1/f0;

% generate x(t)
t = linspace(-T/2, T/2, N);
x1 = cos(2*pi*f0*t);
x2 = rectangularPulse(0, T0, t);
x = x1 .* x2;

% plot x(t) from -T0 to 2*T0
figure();
subplot(3, 3, 1);
plot(t, x);
xlabel('$t$ (s)');
ylabel('$x(t)$');
title('$x(t)$');
xlim([-T0, 2*T0]);

% plot X(f) (magnitude and phase) for entire Nyquist bandwidth
X = fftshift(fft(x)/fs);
wd = linspace(-pi, pi, N);
f = wd * fs / (2 * pi);
subplot(3, 3, 2);
plot(f, abs(X));
xlabel('$f$ (Hz)');
ylabel('$|X(f)|$');
title('Magnitude of $X(f)$');
subplot(3, 3, 3);
plot(f, unwrap(angle(X)));
xlabel('$f$ (Hz)');
ylabel('$\angle X(f)$');
title('(Unwrapped) Phase of $X(f)$');

% compare x norm, X norm (they are equal)
% these both print out 0.25 (T/2) for the given parameters
fprintf('||x||=%d\n||X||=%d\n', trapz(t, abs(x).^2), trapz(f, abs(X).^2));

% Q6

% recompute x, shifted, and compute and plot y(t) from -3-T0 to -3+2*T0
x1Shifted = cos(2*pi*f0*(t+3));
x2Shifted = rectangularPulse(0, T0, (t+3));
xShifted = x1Shifted .* x2Shifted;
y = abs(xShifted);
subplot(3, 3, 4);
plot(t, y);
xlabel('$t$');
ylabel('$y(t)$');
title('$y(t)=|x(t+3)|$');
xlim([-3-T0, -3+2*T0]);

% plot Y(f) (magnitude and phase) for entire Nyquist bandwidth
Y = fftshift(fft(y)/fs);
subplot(3, 3, 5);
plot(f, abs(Y));
xlabel('$f$ (Hz)');
ylabel('$|Y(f)|$');
title('Magnitude of $Y(f)$');
subplot(3, 3, 6);
plot(f, unwrap(angle(Y)));
xlabel('$f$ (Hz)');
ylabel('$\angle Y(f)$');
title('(Unwrapped) Phase of $Y(f)$');

% Q7

% compute z(t) and plot on same x-axis as y(t)
z = y .* cos(64*pi*t + pi/3);
subplot(3, 3, 7);
plot(t, z);
xlabel('$t$');
ylabel('$z(t)$');
title('$z(t)=y(t)\cos(64\pi t+\pi/3)$');
xlim([-3-T0, -3+2*T0]);

% compute and plot Z(f)
Z = fftshift(fft(z)/fs);
subplot(3, 3, 8);
plot(f, abs(Z));
xlabel('$f$ (Hz)');
ylabel('$|Z(f)|$');
title('Magnitude of $Z(f)$');
subplot(3, 3, 9);
plot(f, unwrap(angle(Z)));
xlabel('$f$ (Hz)');
ylabel('$\angle Z(f)$');
title('(Unwrapped) Phase of $Z(f)$');

% Q8-9
innerProd = @(t, f, g) trapz(t, f .* conj(g));
figure();
signals = [x; y; z];
labels = ['x' 'y' 'z'];
for i = 1:3
    signal = signals(i, :);
    subplot(3, 1, i);
    plot(t, signal, t, abs(hilbertTransform(signal)));
    xlabel('t (s)');
    ylabel('signal magnitude');
    legend([string(sprintf('$%c(t)$', labels(i))), ...
        string(sprintf('$\\hat{%c}(t)$', labels(i)))], ...
        'interpreter', 'latex');
    title(sprintf('$%c(t)$ and $\\hat{%c}(t)$', labels(i), labels(i)), ...
        'interpreter', 'latex');
    
    % this should be fairly small (approximately 0) because the signals
    % and their hilbert transforms should be orthogonal
    fprintf('|<%c(t),\\hat{%c}(t)>|=%d\n', labels(i), labels(i), ...
        abs(innerProd(t, signal, hilbertTransform(signal))))
end

% Q8
function res = hilbertTransform(x)
    X = fft(x);
    len = size(x, 2);
    % get signum of frequency; +1 for 0 to pi, -1 for -pi to 0
    sgn = [ones(1, floor(len/2)), -1*ones(1, ceil(len/2))];
    res = ifft(-1j * sgn .* X);
end