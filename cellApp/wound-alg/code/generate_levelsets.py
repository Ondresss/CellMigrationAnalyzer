"""
    This python code demonstrates image processing and part of statistical analysis described in the paper
            
            Evaluating Wound Healing Assays: A new analyses method for an old experiment

The script performs a demonstration calculation on a single frame (due to time constraints of the Code Ocean environment). This capsule also contains the remaining frames from one experiment. 

Authors: Marketa Vasinkova, Michal Krumnikl, Arootin Gharibian, Petr Gajdos, Eva Kriegova

E-mail: michal.krumnikl@vsb.cz

Released Under MIT License
"""

import numpy as np
import pandas as pd
import argparse
import os
from track_util import save_interframe_directions
from skimage.io import imread
from find_lsf import find_lsf
from potential_func import DOUBLE_WELL, SINGLE_WELL
from show_fig import save_figure

def params(image,no_outer_iter):
    img = imread(image)
    img = np.interp(img, [np.min(img), np.max(img)], [0, 255])

    # initialize LSF as binary step function
    c0 = 2
    initial_lsf = c0 * np.ones(img.shape)

    # generate the initial region R0 as two rectangles
    initial_lsf[0:904, 0:550] = -c0
    initial_lsf[0:904, 600:1224] = -c0

    # parameters
    return {
        'img': img,
        'initial_lsf': initial_lsf,
        'timestep': 5,  # time step
        'iter_inner': 5,
        'iter_outer': no_outer_iter,
        'lmda': 15,  # coefficient of the weighted length term L(phi)
        'alfa': 1.5,  # coefficient of the weighted area term A(phi)
        'epsilon': 3.5,  # parameter that specifies the width of the DiracDelta function
        'sigma': 1.5,  # scale parameter in Gaussian kernel
        'potential_function': SINGLE_WELL,
    }

parser = argparse.ArgumentParser(description='Image processing for wound healing analysis.')
parser.add_argument('resultDir', type=str, help='Path to the result direction')
parser.add_argument('iter_outer', type=int, help='Number of iterations for the outer loop')
args = parser.parse_args()
brightFieldDir = args.resultDir + '/BrightField'
dataDir = args.resultDir + '/DATA'

if os.path.exists(brightFieldDir) and os.path.isdir(brightFieldDir):
        for filename in os.listdir(brightFieldDir):
            print("Calculating levelset ...")
            param = params(os.path.join(brightFieldDir, filename),args.iter_outer)
            phi = find_lsf(**param)
            np.save(os.path.join(dataDir, f"{filename.split('.')[0]}.npy"),phi)
            save_figure(phi, param['img'], os.path.join(dataDir, f"{filename.split('.')[0]}.tif"))
            print("Levelset finished")

