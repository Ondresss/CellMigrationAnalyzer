"""
    This python code demonstrates image processing and part of statistical analysis described in the paper
	    
	    Evaluating Wound Healing Assays: A new analyses method for an old experiment

Authors: Marketa Vasinkova, Michal Krumnikl, Arootin Gharibian, Petr Gajdos, Eva Kriegova

E-mail: michal.krumnikl@vsb.cz

Released Under MIT License
"""


import argparse
import pandas as pd
from track_util import save_interframe_directions


# Initialize parser
parser = argparse.ArgumentParser()

# Adding optional argument
parser.add_argument("-i", "--InputImg", help = "Input images (xxx number will be replaced from 001 - 070)")
parser.add_argument("-l", "--InputLevelset", help = "Input levelset (.npy) (xxx number will be replaced from 001 - 070)")
parser.add_argument("-s", "--Spots", help = "Spots datafile (.csv)")
parser.add_argument("-t", "--Tracks", help = "Tracks datafile (.csv)")
parser.add_argument("-o", "--OutputImg", help = "Output image file (.png or .pdf)")
parser.add_argument("-from","--FromFrame", help = "First frame (0)")
parser.add_argument("-to","--ToFrame", help = "Last frame (69)")
parser.add_argument("-ss","--StepSize", help = "Step between frames")

# Read arguments from command line
args = parser.parse_args()

print("Processing " + str(args.InputImg))

df_spots = pd.read_csv(args.Spots)
df_tracks = pd.read_csv(args.Tracks)      
save_interframe_directions(df_spots, df_tracks, args.InputImg, args.InputLevelset, args.OutputImg,args.FromFrame,args.ToFrame,args.StepSize)

print("Finished, files saved")
