clc;clear all;

y = [ -1; -1; -1; -1; -1; 1; 1; 1; 1; 1; 1 ];
data = [
    0 3;
    1 2;
    2 1;
    3 3;
    5 1;
    5 5;
    6 5;
    6 7;
    7 3;
    8 6;
    8 1;
];

% separate positive and negative datapoints
posDps = data(find(y == 1), :);
negDps = data(find(y == -1), :);

% space is number of dimensions (n-space)
% right now, b/c being plotted in 2D, can only be 2-space
space = size(data, 2);
% n is number of input variables
n = length(data);
[ y1, y2 ] = meshgrid(y, y);
[ i, j ] = meshgrid(1:n, 1:n);

% generate matrices for minimization
P(i, j) = y1(i, j) .* y2(i, j) .* (data(i,:) * data(j,:)');
q = -1 * ones(n, 1);

% generate matrices for inequality constraint (alpha >= 0)
% also can use LB parameter
A = -1 * eye(n);
b = zeros(n, 1);

% generate matrices for equality constraint
Aeq = y';
beq = [ 0 ];

% use quadprog package to generate alphas
alpha = quadprog(P, q, A, b, Aeq, beq, [], [], [], optimoptions('quadprog', 'Display', 'off'));
% calculate w from alpha
w = (data' .* repmat(y', [space 1])) * alpha;

% finding b parameter
% (y_n)(x_n * w + b) = 1 for support vector, so b = y_n - w * x_n
threshold = 1e-5;
svIndices = find(alpha > threshold);
b = y(svIndices(1)) - data(svIndices(1),:) * w;

% display points
figure;
hold on;
scatter(posDps(:,1), posDps(:,2));
scatter(negDps(:,1), negDps(:,2));

% draw line (only 2D for now)
margin = 1;
domain = (min(data(:,1)) - margin):(max(data(:,1)) + margin);
plot(domain, (w(1) .* domain + b)/(-w(2)));

% plotting gutters
plot(domain, (-1 + w(1) .* domain + b)/(-w(2)), 'g:');
plot(domain, (1 + w(1) .* domain + b)/(-w(2)), 'g:');