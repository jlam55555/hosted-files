% demonstration of RK4 method
clc;clear all;

syms t y
f = @(t, y) 2 * y^2 + 7 / t * y;
ft = @(t, y) 7 * y * t^-2;
fy = @(t, y) 4 * y + 7 / t;

ti = 3.5;
tf = 10;
wi = -1;
h = 0.25;

% actual
figure;
x = ti:h:tf;
%plot(x, sin(x * y));
hold on;

% euler
[x, y] = euler(ti, tf, wi, h, f);
plot(x, y);

% modified euler
[x, y] = rk4(ti, tf, wi, h, f, ft, fy, 0, 1, h/2, h/2);
plot(x, y);

% heum
[x, y] = rk4(ti, tf, wi, h, f, ft, fy, 0.5, 0.5, h, h);
plot(x, y);

% optimal
[x, y] = rk4(ti, tf, wi, h, f, ft, fy, 1/4, 3/4, 2*h/3, 2*h/3);
plot(x, y);

legend('Euler', 'Modified Euler', 'Heum', 'Optimal');

function [t, w] = euler(ti, tf, wi, h, f)
    t = ti:h:tf;
    w(1) = wi;
    for k = 2:length(t)
        w(k) = w(k-1) + h * f(t(k-1), w(k-1));
    end
end

function [t, w] = rk4(ti, tf, wi, h, f, ft, fy, a1, a2, alpha, delta)
    t = ti:h:tf;
    w(1) = wi;
    for k = 2:length(t)
        w(k) = w(k-1) + h * ((a1 + a2) * f(t(k-1), w(k-1)) + a2 * alpha * ft(t(k-1), w(k-1)) + a2 * delta * f(t(k-1), w(k-1)) * fy(t(k-1), w(k-1)));
    end
end