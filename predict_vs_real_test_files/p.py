import math
from math import exp, sin, cos, log

input = 26
range = "7062, 7350"
actual_min = float(range.split(', ')[0])
actual_max = float(range.split(', ')[1])

predict_checkTree = (input * 1.7818553e-7) * ((input * input) + 5.4003377)
predict_createTree = exp((input + -19.571716) * 0.6815255) + -0.08256852
predict_trees = exp((input + -11.710805) * 0.563765)                #exp((input + -10.852579) * 0.51566976) + -3.9919426


predict_energy = 4.2848456e-5
predict_advance = (-7.739528e-6 * sin(input * -0.81902814)) + 4.2258347e-5
#predict_fannkuchredux = exp((input + -9.133306) * 2.4944384) + 0.003643311
predict_approximate = sin((input * 2.0593527e-6) + 0.0006085703) * input

#predict_for_trees_best_model = (((math.exp(1) * math.exp((math.cos(0.5088351 * (input + -16.912373)) + -16.321354) + input)) + math.sin(math.exp(math.cos(0.92523444 + input)))) + math.sin(math.exp(0.92523444 + math.cos(input)))) + 0
#predict_for_createTree_best_model = math.exp(((((math.sin(1) * input) + math.exp((math.exp(1 * (math.sin(math.sin(input + -0.24880834) + -0.09010311) * 2.2742789)) + -0.04007355) * -14.436747)) + -13.556862) * 0.8816005) + -2.9527016)
#predict_for_checkTree_best_model = (((math.sin(math.sin(input) * (input + 1.4366353)) + input) * ((input + 1.2719605) * (math.sin(math.exp((math.sin(input) * 0.64363307) + 1.2281508)) + input))) * 1.900417e-7) + -2.1762844e-6


predict = predict_createTree + predict_checkTree + predict_trees

#predict_for_checkTree = (input * 1.7818553e-7) * ((input * input) + 5.4003377)

actual_avg = (actual_max+actual_min) / 2 

#print("f",f)

error = (predict-actual_avg)/actual_avg*100

def smart_format(x):
    return f"{x:.2e}" if abs(x) < 0.01 else f"{x:.2f}"

if error < 0:error *=-1
#print(f'predict checkTree {predict_for_checkTree}')
print(f'Error: {error}')
print(f"predict for in {input} is {smart_format(predict)}")
print(f'predict unformated -> {predict}')
#print("avg",actual_avg)

