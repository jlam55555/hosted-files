import numpy as np
from math import sqrt

mu_c = 200e-6   # mu_n * C_{ox}
wol = 15        # W/L
v_th = 2        # V_{TH}
lam = 0.02      # lambda

r_1 = 55e3
r_d = 40e3
r_d2 = 4e3
r_5 = 6e3

v_dd = 12
v_ss = -12

# finding i_d4, v_gs4
i_d4 = 1e-3     # I_{D,4}
v_gs4 = 2.1     # V_{GS,4} = V_{DS,4}

def iterate():
    global i_d4, v_gs4
    i_d4 = (v_dd - v_gs4 - v_ss) / r_1
    v_gs4 = v_th + sqrt(2*i_d4/(wol * mu_c * (1 + lam * v_gs4)))
    print(f'i_d4 {i_d4} v_gs4 {v_gs4}')

print(f'i_d4 {i_d4} v_gs4 {v_gs4}')
for _ in range(5):
    iterate()

# finding v_gs1, v_gs2
i_d1 = i_d4/2
v_d1 = 12-r_d*i_d1
v_gs1 = 2.1

def iterate():
    global v_gs1
    v_ds1 = v_d1 + v_gs1
    v_gs1 = v_th + sqrt(2 * i_d1 / (mu_c * wol * (1 + lam * v_ds1)))
    print(f'v_ds1 {v_ds1} v_gs1 {v_gs1}')

print(f'v_gs1 {v_gs1}')
for _ in range(5):
    iterate()

# finding i_d3, v_gs3
v_g3 = v_d1
i_d3 = 1e-3
v_gs3 = 2.1

def iterate():
    global i_d3, v_gs3
    i_d3 = (v_g3 - v_gs3) / r_5
    v_d3 = v_dd - i_d3 * r_d2
    v_s3 = v_g3 - v_gs3
    v_ds3 = v_d3 - v_s3
    v_gs3 = v_th + sqrt(2 * i_d3 / (mu_c * wol * (1 + lam * v_ds3)))
    print(f'i_d3 {i_d3} v_gs3 {v_gs3}')

print(f'i_d3 {i_d3} v_gs3 {v_gs3}')
for _ in range(5):
    iterate()
