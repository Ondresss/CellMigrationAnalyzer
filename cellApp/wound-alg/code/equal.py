import numpy as np

file1 = "/home/andrew/cellApp/wound-alg/results/A2_03_1_1_BrightField0001.npy"
file2 = "/home/andrew/cellApp/wound-alg/results/A2_03_1_1_BrightField0050.npy"

data1 = np.load(file1)
data2 = np.load(file2)

if np.array_equal(data1, data2):
    print("Soubory jsou IDENTICKÉ.")
else:
    print("Soubory se LIŠÍ.")
