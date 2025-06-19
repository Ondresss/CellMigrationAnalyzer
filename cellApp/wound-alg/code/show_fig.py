"""
    This python code demonstrates image processing and part of statistical analysis described in the paper
            
            Evaluating Wound Healing Assays: A new analyses method for an old experiment

Authors: Marketa Vasinkova, Michal Krumnikl, Arootin Gharibian, Petr Gajdos, Eva Kriegova

E-mail: michal.krumnikl@vsb.cz

Released Under MIT License
"""

import matplotlib.pyplot as plt
import numpy as np
from skimage import measure
import cv2

import matplotlib.offsetbox
from matplotlib.lines import Line2D


""" AnchoredHScalerBar modified based on https://stackoverflow.com/a/70896260 """
class AnchoredHScaleBar(matplotlib.offsetbox.AnchoredOffsetbox):
    """ size: length of bar in data units
        extent : height of bar ends in axes units """
    def __init__(self, size=1, extent = 0.03, label="", loc=2, ax=None,
                 pad=0.4, borderpad=0.5, ppad = 0, sep=2, prop=None, 
                 frameon=True, linekw={}, **kwargs):
        if not ax:
            ax = plt.gca()
        trans = ax.get_xaxis_transform()
        size_bar = matplotlib.offsetbox.AuxTransformBox(trans)
        line = Line2D([0,size],[0,0], **linekw)
        vline1 = Line2D([0,0],[-extent/2.,extent/2.], **linekw)
        vline2 = Line2D([size,size],[-extent/2.,extent/2.], **linekw)
        size_bar.add_artist(line)
        size_bar.add_artist(vline1)
        size_bar.add_artist(vline2)
        txt = matplotlib.offsetbox.TextArea(label)
        self.vpac = matplotlib.offsetbox.VPacker(children=[size_bar,txt],  
                                 align="center", pad=ppad, sep=sep) 
        matplotlib.offsetbox.AnchoredOffsetbox.__init__(self, loc, pad=pad, 
                 borderpad=borderpad, child=self.vpac, prop=prop, frameon=frameon,
                 **kwargs)

 
def save_figure(phi: np.ndarray, img: np.ndarray, path_out: str):
    plt.ion()
    fig2 = plt.figure(2)
    fig2.clf()
    contours = measure.find_contours(phi, 0)

    ax2 = fig2.add_subplot(111)
    ax2.imshow(img, interpolation='nearest', cmap=plt.get_cmap('gray'))
    
    areas = []
    cv_contours = []
    for n, contour in enumerate(contours):
        c = np.expand_dims(contour.astype(np.float32), 1)
        c = cv2.UMat(c)
        cv_contours.append(c)
        areas.append(cv2.arcLength(c,True))
            
    max_index_1 = np.argsort(areas)[-1]
    max_index_2 = np.argsort(areas)[-2]

    max_contour_1 = contours[max_index_1]
    max_contour_2 = contours[max_index_2]
        
    ax2.plot(max_contour_1[:, 1], max_contour_1[:, 0], linewidth=2, color='orange')
    ax2.plot(max_contour_2[:, 1], max_contour_2[:, 0], linewidth=2, color='orange')
    
    px_to_microns = 1.64787581699
    measure_label = '100 µm'
    measure_length_pixels = 100 / px_to_microns  # Adjust according to your image
    ob = AnchoredHScaleBar(size=measure_length_pixels, label=measure_label, loc=4, frameon=True, pad=0.6,sep=4, linekw=dict(color="black")) 
    ax2.add_artist(ob)
    
    ax2.tick_params(axis='x', which='both', bottom=False, top=False, labelbottom=False)
    ax2.tick_params(axis='y', which='both', left=False, right=False, labelleft=False)

    fig2.savefig(path_out, dpi=300)
    
