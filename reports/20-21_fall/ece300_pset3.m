% Jonathan Lam
% Prof. Frost
% ECE300
% Communications Theory
% 9/25/20

clear; close all; clc; 
set(0, 'defaultTextInterpreter', 'latex');

% Q7
N = 100000;                         % number of samples
M = 1000;                           % autocorrelation range
n_variance = 32;                    % variance of white noise
w = -10000:10000;                   % frequency axis for PSD

% generate white noise, plot it
n_sig = randn(1, N) * sqrt(n_variance);
subplot(3, 1, 1);
plot(n_sig);
title('White noise');
ylabel('$n(t)$');
xlabel('$t$');

% compute and plot autocorrelation of white noise
m = -M:M;
R_N = arrayfun(@(m) autocorr(n_sig, m), m);
subplot(3, 1, 2);
semilogy(m, abs(R_N));
title('White noise autocorrelation');
ylabel('$R_N(m)$');
xlabel('$m$ (delay)');

% compute and plot PSD of white noise, plot it
S_N = psd(n_sig, M, w);
subplot(3, 1, 3);
semilogy(w, abs(S_N));
title('White noise PSD');
ylabel('$S_N(\omega)$');
xlabel('$\omega$ (rad)');

% Q8
w_0 = 10000;                        % cosine frequency (Q8)
YZ_var = 64;                        % variance for Y and Z RVs (Q8)

% X(t) = Y*cos(w_0*t) - Z*sin(w_0*t), w_0 = 10000rad/s
t = 0:1/N:1;
Y = randn(size(t)) * sqrt(YZ_var);
Z = randn(size(t)) * sqrt(YZ_var);
X = Y .* cos(w_0 * t) - Z .* sin(w_0 * t);

% compute and plot PSD of X
S_X = psd(X, M, w);
figure();
subplot(2, 1, 1);
semilogy(w, abs(S_X));
title('PSD of $X$');
ylabel('$S_X(\omega)$');
xlabel('$\omega$ (rad)');

% compute and plot PSD of integral of X
S_Y = S_X ./ (w .^ 2);
subplot(2, 1, 2);
semilogy(w, abs(S_Y));
title('PSD of $\int X\,dx$');
ylabel('$S_{\int X\,dx}(\omega)$');
xlabel('$\omega$ (rad)');

% X: 1xN
% m: scalar
% returns: scalar
function res = autocorr(X, m)
    m = abs(m);
    N = length(X);
    res = X(1:N-m) * X(m+1:N).' / (N - m);
end

% X: 1xN
% M: scalar
% w: 1xW
% returns: 1xW
function res = psd(X, M, w)
    m = -M:M;
    res = arrayfun(@(m) autocorr(X, m), m) * exp(-1j * w .* m.' / (2 * M + 1));
end