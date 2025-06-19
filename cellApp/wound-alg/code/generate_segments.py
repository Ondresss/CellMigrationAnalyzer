import numpy as np
import pandas as pd
import argparse
import os
from track_util import save_interframe_directions
import re

def parse_args():
    parser = argparse.ArgumentParser(description='Image processing for wound healing analysis.')
    parser.add_argument('resultDir', type=str, help='Path to the result directory')
    parser.add_argument('step_size', type=int, help='Number of skipped images')
    parser.add_argument('no_frames', type=int, help='Number of frames')
    return parser.parse_args()


def find_minimum_file(dir_path, ext):
    min_num = float('inf')
    file_name = None
    for file in os.listdir(dir_path):
        if file.endswith(ext):
            match = re.search(r'(\d{4,})', file)
            if match:
                num = int(match.group(1))  
                if num < min_num:
                    min_num = num
                    file_name = file
    return file_name


def validate_directories(dirs):
    for key, path in dirs.items():
        if not os.path.exists(path) or not os.path.isdir(path):
            raise ValueError(f"{key} directory {path} does not exist or is not a directory.")

def locate_trackmate_files(trackmate_dir):
    trackmate_csv = {"track_file": None, "spot_file": None}
    for file in os.listdir(trackmate_dir):
        if 'spots' in file and file.endswith('.csv'):
            trackmate_csv["spot_file"] = os.path.join(trackmate_dir, file)
        elif 'tracks' in file and file.endswith('.csv'):
            trackmate_csv["track_file"] = os.path.join(trackmate_dir, file)
    if trackmate_csv["spot_file"] is None or trackmate_csv["track_file"] is None:
        raise ValueError("Spot or track file not found in TRACKMATE directory.")
    
    trackmate_csv['spot_file'] = pd.read_csv(trackmate_csv['spot_file'])
    trackmate_csv['track_file'] = pd.read_csv(trackmate_csv['track_file'])
    return trackmate_csv

def main():
    args = parse_args()

    dirs = {
        'BrightField': os.path.join(args.resultDir, 'BrightField'),
        'DAPI': os.path.join(args.resultDir, 'DAPI'),
        'DATA': os.path.join(args.resultDir, 'DATA'),
        'TRACKMATE': os.path.join(args.resultDir, 'TRACKMATE'),
        'TRACKS': os.path.join(args.resultDir, 'TRACKS'),
    }

    validate_directories(dirs)
    trackmate_csv = locate_trackmate_files(dirs['TRACKMATE'])

    minimum_dapi = find_minimum_file(dirs['DAPI'], '.tif')
    minimum_npy = find_minimum_file(dirs['DATA'], '.npy')

    if minimum_dapi is None:
        raise ValueError(f"No .tif files found in {dirs['DAPI']}")
    if minimum_npy is None:
        raise ValueError(f"No .npy files found in {dirs['DATA']}")

    minimum_dapi_path = os.path.join(dirs['DAPI'], minimum_dapi)
    minimum_npy_path = os.path.join(dirs['DATA'], minimum_npy)

    output_result = f"A2_03_2_1_DAPI_{minimum_dapi}"
    output_result = os.path.join(dirs['TRACKS'], output_result)
    print("Calculating region tracking ...")
    save_interframe_directions(
        trackmate_csv['spot_file'],
        trackmate_csv['track_file'],
        minimum_dapi_path,
        minimum_npy_path,
        output_result,
        0,
        args.step_size,
        args.no_frames
    )
    print("Region tracking finished.")

if __name__ == "__main__":
    main()
