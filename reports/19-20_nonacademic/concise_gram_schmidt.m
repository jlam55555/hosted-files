clc; clear all;

% helpers
global inprod normalize
inprod = @(x, y) transpose(x) * conj(y);
normalize = @(x) x./sqrt(inprod(x, x));
isorth = @(arr) all(all(inprod(arr, arr)-eye(size(arr,2))<0.001));

% params
vects = 500;
dimension = 500;

% testing code
vs1 = rand(vects,dimension)+rand(vects,dimension)*i;
io1 = isorth(vs1)
vs2 = gs(vs1);
io2 = isorth(vs2)

% gs
function [v] = gs(v)
    global inprod normalize
    for j = 1:size(v, 2)
        v(:,j) = normalize(v(:,j)-sum(repmat(inprod(v(:,j),v(:,1:j-1)),[size(v,1) 1]).*v(:,1:j-1), 2));
    end
end