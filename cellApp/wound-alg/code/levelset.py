"""
This python code demonstrates an edge-based active contour model as an application of the
Distance Regularized Level Set Evolution (DRLSE) formulation in the following paper:

  C. Li, C. Xu, C. Gui, M. D. Fox, "Distance Regularized Level Set Evolution and Its Application to Image Segmentation",
     IEEE Trans. Image Processing, vol. 19 (12), pp. 3243-3254, 2010.

Author: Ramesh Pramuditha Rathnayake
E-mail: rsoft.ramesh@gmail.com

Released Under MIT License
"""
import numpy as np
from skimage.io import imread

from find_lsf import find_lsf
from potential_func import DOUBLE_WELL, SINGLE_WELL
from show_fig import save_figure
import argparse


def params(image):
    img = imread(image)
    img = np.interp(img, [np.min(img), np.max(img)], [0, 255])

    # initialize LSF as binary step function
    c0 = 2
    initial_lsf = c0 * np.ones(img.shape)

    # generate the initial region R0 as two rectangles
    #initial_lsf[0:480, 0:315] = -c0
    #initial_lsf[0:480, 316:800] = -c0
    initial_lsf[0:904, 0:550] = -c0
    initial_lsf[0:904, 600:1224] = -c0

    # parameters
    return {
        'img': img,
        'initial_lsf': initial_lsf,
        'timestep': 5,  # time step
        'iter_inner': 5,
        'iter_outer': 500,
        'lmda': 15,  # coefficient of the weighted length term L(phi)
        'alfa': 1.5,  # coefficient of the weighted area term A(phi)
        'epsilon': 3.5,  # parameter that specifies the width of the DiracDelta function
        'sigma': 1.5,  # scale parameter in Gaussian kernel
        'potential_function': SINGLE_WELL,
    }

# Initialize parser
parser = argparse.ArgumentParser()

# Adding optional argument
parser.add_argument("-i", "--Input", help = "Input file")
parser.add_argument("-ophi", "--OutputPhi", help = "Output phi file (.npy)")
parser.add_argument("-oimg", "--OutputImg", help = "Output image file (.png or .pdf)")


# Read arguments from command line
args = parser.parse_args()


print("Processing " + str(args.Input))
param = params(args.Input)
phi = find_lsf(**param)
np.save(args.OutputPhi,phi)
save_figure(phi, param['img'], args.OutputImg)
print("Finished, files saved")
