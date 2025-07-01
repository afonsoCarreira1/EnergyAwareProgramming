import math


input = 10
actual_min = 4.59e-2 
actual_max = 4.77e-2
predict_for_checkTree = (input * 1.7818553e-7) * ((input * input) + 5.4003377)
predict = -3.5 + predict_for_checkTree

predict_for_checkTree = (input * 1.7818553e-7) * ((input * input) + 5.4003377)

actual_avg = (actual_max+actual_min) / 2 

#f = (((math.exp(1) * math.exp((math.cos(0.5088351 * (input + -16.912373)) + -16.321354) + input)) + math.sin(math.exp(math.cos(0.92523444 + input)))) + math.sin(math.exp(0.92523444 + math.cos(input)))) + 0
#print("f",f)

error = (predict-actual_avg)/actual_avg*100

if error < 0:error *=-1
print(f'predict checkTree {predict_for_checkTree}')
print(f'Error: {error}')
print(f"predict for in {input} is {predict}")
#print("avg",actual_avg)