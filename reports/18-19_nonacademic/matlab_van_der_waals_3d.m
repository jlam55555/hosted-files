clc; clear all; close all;

r = 0.08206;
a = 3.658;
b = 0.04286;

P = 1;
V = 22.4;
T = 290:320;

figure;
subplot(2, 2, 1);
vRange = linspace(0.08, 20, 1000);
tRange = linspace(1, 300, 100);
pRange = linspace(1, 20, 100);
[v, t] = meshgrid(vRange, tRange);
p = (r .* t) ./ (v - b) - a .* v.^-2;
surface(p);
xlabel('V');
ylabel('T');
zlabel('P');

[vv, tt] = meshgrid(vRange, 200:20:400);
subplot(2, 2, 2);
pov = (r .* tt) ./ (vv - b) - a .* vv.^-2;
plot(vRange, pov);
xlabel('V');
ylabel('P');

subplot(2, 2, 3);
top = (pRange + a * V^-2) .* (V - b) / r;
plot(pRange, top);
xlabel('T');
ylabel('P');

subplot(2, 2, 4);
tov = (P + a * vRange.^-2) .* (vRange - b) ./ r;
plot(vRange, tov);
xlabel('T');
ylabel('V');