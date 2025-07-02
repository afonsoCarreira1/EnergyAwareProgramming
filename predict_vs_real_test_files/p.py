import math


input = 10
range = "3.75e-4 -- 4.60e-4"
actual_min = float(range.split(' -- ')[0])
actual_max = float(range.split(' -- ')[1])

predict_for_checkTree = (input * 1.7818553e-7) * ((input * input) + 5.4003377)
predict_for_trees_best_model = (((math.exp(1) * math.exp((math.cos(0.5088351 * (input + -16.912373)) + -16.321354) + input)) + math.sin(math.exp(math.cos(0.92523444 + input)))) + math.sin(math.exp(0.92523444 + math.cos(input)))) + 0
predict_for_createTree_best_model = math.exp(((((math.sin(1) * input) + math.exp((math.exp(1 * (math.sin(math.sin(input + -0.24880834) + -0.09010311) * 2.2742789)) + -0.04007355) * -14.436747)) + -13.556862) * 0.8816005) + -2.9527016)
predict_for_checkTree_best_model = (((math.sin(math.sin(input) * (input + 1.4366353)) + input) * ((input + 1.2719605) * (math.sin(math.exp((math.sin(input) * 0.64363307) + 1.2281508)) + input))) * 1.900417e-7) + -2.1762844e-6
predict = -.083

predict_for_checkTree = (input * 1.7818553e-7) * ((input * input) + 5.4003377)

actual_avg = (actual_max+actual_min) / 2 

#print("f",f)

error = (predict-actual_avg)/actual_avg*100

def smart_format(x):
    return f"{x:.2e}" if abs(x) < 0.01 else f"{x:.2f}"

if error < 0:error *=-1
#print(f'predict checkTree {predict_for_checkTree}')
print(f'Error: {error}')
print(f"predict for in {input} is {smart_format(predict)}")
#print("avg",actual_avg)

