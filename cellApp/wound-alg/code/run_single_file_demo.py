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
from track_util import save_interframe_directions
from skimage.io import imread
from find_lsf import find_lsf
from potential_func import DOUBLE_WELL, SINGLE_WELL
from show_fig import save_figure

def params(image):
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
        'iter_outer': 500,
        'lmda': 15,  # coefficient of the weighted length term L(phi)
        'alfa': 1.5,  # coefficient of the weighted area term A(phi)
        'epsilon': 3.5,  # parameter that specifies the width of the DiracDelta function
        'sigma': 1.5,  # scale parameter in Gaussian kernel
        'potential_function': SINGLE_WELL,
    }

#print("Calculating levelset ...")
#param = params('../data/BrightField/A2_03_1_1_BrightField0050.tif')
#phi = find_lsf(**param)
#np.save('../results/A2_03_1_1_BrightField0050.npy',phi)
#save_figure(phi, param['img'], '../results/A2_03_1_1_BrightField0050.tif')
#print("Levelset finished")

#trackmate ma na vstup DAPI obrazky
#TrackMate a potom detektor se jmenuje StarDist
#Kouknout sse na python kod na github
#Pridat
#Uzivatel ma na vyber co z kodu ma pouzit
#Popsat jak vznikala aplikace a nezabyvat (Myslenky a Technicky) a veci diagramy z SWI
#prvni a posledni obrazen do save_interframe_directories, step-size kolikaty obrazek se ma vzit, moznost kolikaty obrazek vzit i co se tyce levelsetu
#plus zakomponovat ten fiji plugin
#dat tam i parametry navic pro ten levelSetu
#Zkoumat jednotlive vztahy na zaklade charakteru lokalni hustoty bunek

print("Calculating region tracking ...")
# Spots and tracks files are obtained from Fiji plugin TrackMate https://imagej.net/plugins/trackmate/detectors/trackmate-stardist 
df_spots = pd.read_csv('../data/Tracks/A2_03_2_1_Stab_spots.csv')
df_tracks = pd.read_csv('../data/Tracks/A2_03_2_1_Stab_tracks.csv')      
save_interframe_directions(df_spots, df_tracks, '../data/DAPI/A2_03_2_1_DAPI0001.tif', '../results/A2_03_1_1_BrightField0001.npy', '../results/A2_03_2_1_DAPI0001.tif',0,2,1)
print("Region tracking finished")
