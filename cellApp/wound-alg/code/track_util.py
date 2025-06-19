"""
    This python code demonstrates image processing and part of statistical analysis described in the paper
            
            Evaluating Wound Healing Assays: A new analyses method for an old experiment

Authors: Marketa Vasinkova, Michal Krumnikl, Arootin Gharibian, Petr Gajdos, Eva Kriegova

E-mail: michal.krumnikl@vsb.cz

Released Under MIT License
"""


import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import numpy as np
import warnings
warnings.simplefilter(action='ignore', category=FutureWarning)
import pandas as pd
import math
from skimage import measure
import cv2
import csv
import re
import sys
import matplotlib.colors as colors

import matplotlib.patches as patches
from show_fig import AnchoredHScaleBar

def calc_contour_avg(contours, height):
    no_arrows = 5
    x_coord = []
    for contour in contours:
        for x in range(0, no_arrows):
            x_coord.append([])
            if (((height / no_arrows) * x) <= contour[0] <= ((height / no_arrows) * (x+1))):
                x_coord[x].append(contour[1])
    
    ret = []                                                                  
    for x in range(0, no_arrows):
        ret.append(np.mean(x_coord[x]))

    return ret

def normalize_vector(vector, desired_length):
    current_length = np.linalg.norm(vector)  
    normalized_vector = (vector / current_length) * desired_length  
    return normalized_vector

def replace_frame_digits(text, frame):
    val = re.sub(r"\d{4}(?=\.)", lambda m: str(frame).zfill(4), text)
    print("Replaced called",val);
    return val

def difSigns(x, y): 
    return (y >= 0) if (x < 0) else (y < 0);

def save_interframe_directions(df_spots, df_tracks, image_names, levelset_names, output_names, from_frame,to_frame,step_size):
    plt.clf()
    df_tracks_sorted = df_tracks.sort_values(by='TOTAL_DISTANCE_TRAVELED', ascending=False)
    image = mpimg.imread(image_names)
    height, width = image.shape

    #for id in df_tracks['TRACK_ID']:
    #    try:
    #        track = df_spots[df_spots['TRACK_ID'] == id]
    #        sposx_int = track['POSITION_X'].astype(int).iloc[0] #first
    #        eposx_int = track['POSITION_X'].astype(int).iloc[-1] #last
    #        df_tracks['XDIFF'] = abs(sposx_int - eposx_int)
    #    except:
    #        df_tracks['XDIFF'] = 0
    #df_tracks_sorted = df_tracks.sort_values(by='XDIFF', ascending=False)
    
    #df_tracks_sorted = df_tracks.sort_values(by='TOTAL_DISTANCE_TRAVELED', ascending=False)
    df_tracks_sorted = df_tracks.sort_values(by='TRACK_DISPLACEMENT', ascending=False)

    top_100 = df_tracks_sorted.head(100)
    top_tracks = df_tracks_sorted;

    unique_ids = top_tracks['TRACK_ID'].unique()
    
    color = 0
    boxes_x = 10 #20
    boxes_y = 12 #24
    start_t = int(from_frame)  #0  # first frame
    stop_t = int(to_frame) # 69  # last frame
    
    cmap = plt.cm.get_cmap('RdYlGn') # Greens
    norm = colors.Normalize(vmin=-5, vmax=5)  # Normalize the values
    sm = plt.cm.ScalarMappable(norm=norm, cmap=cmap)  # Create a ScalarMappable object

    for frame in range(start_t,stop_t,int(step_size)):
        count = 0
        fig, ax = plt.subplots()
        phi = np.load(replace_frame_digits(levelset_names, frame))
        top_df = pd.DataFrame()
        csv_file = open(replace_frame_digits(output_names, frame).replace(output_names[len(output_names) - 4:], '.csv'), 'w')
        csv_file.write('x,y,center_x,center_y,avg_top_cells_vec_x,avg_top_cells_vec_y,top_cells,avg_vec_x,avg_vec_y,count,avg_normal_vec_x,avg_normal_vec_y,angle,box_boundary_distance\n')
        print("")
        for x in range(0, boxes_x):
            for y in range(0, boxes_y):
                count = 1
                displacement = 0.0
                top_cells = 0
                avg_x = 0.0
                avg_y = 0.0
                avg_fastest_x = 0.0
                avg_fastest_y = 0.0
                avg_normal_x = 0.0
                avg_normal_y = 0.0
                
                for id in unique_ids:
                    try:
                        track = df_spots[df_spots['TRACK_ID'] == id]
                        start_track = track[track['POSITION_T'].astype(int) == frame].copy()
                        end_track = track[track['POSITION_T'].astype(int) == (frame + 1)]
                        posx_int = start_track['POSITION_X'].astype(int).iloc[0]
                        posy_int = start_track['POSITION_Y'].astype(int).iloc[0]
                        posx_int_tplus = end_track['POSITION_X'].astype(int).iloc[0]
                        posy_int_tplus = end_track['POSITION_Y'].astype(int).iloc[0]
                        
                        if (posx_int > ((width/boxes_x) * x) and posx_int < ((width/boxes_x) * (x + 1))):
                            if (posy_int > ((height/boxes_y) * y) and posy_int < ((width/boxes_y) * (y + 1))):
                                ax.plot(posx_int, posy_int, marker='o', color = '0.3', alpha=0.3, markersize=1) 
                                count = count + 1
                                avg_x = avg_x + (posx_int_tplus - posx_int)
                                avg_y = avg_y + (posy_int_tplus - posy_int)
                                if (id in top_100['TRACK_ID']):
                                    top_cells = top_cells + 1
                                    avg_fastest_x = avg_fastest_x + (posx_int_tplus - posx_int)
                                    avg_fastest_y = avg_fastest_y + (posy_int_tplus - posy_int)
                                    try:
                                        crossings = np.array([])
                                        for ls_x in range(0,width-1):
                                            if (difSigns(phi[posy_int, ls_x ],phi[posy_int, ls_x + 1])):
                                                #print(str(ls_x) + ", " + str(posy_int))
                                                crossings = np.append(crossings, int(ls_x))
                                        difference_array = np.absolute(crossings-posx_int)
                                        nearest_index = difference_array.argmin()
                                        #print("nereaset crossing x = " + str(difference_array[nearest_index]))
                                        
                                        start_track['BOUNDARY_DIST'] = difference_array[nearest_index]
                                        top_df = pd.concat([top_df, start_track], ignore_index=True)

                                    except Exception as e:
                                        print(e)
                                else:
                                    avg_normal_x = avg_normal_x + (posx_int_tplus - posx_int)
                                    avg_normal_y = avg_normal_y + (posy_int_tplus - posy_int)
                                
                    except:
                        pass
                
                coords = normalize_vector(np.array([avg_x/count , avg_y/count]), 12)
                angle = math.atan2(coords[0], coords[1])
                if (angle > 0 and angle < math.pi and x < boxes_x/2):
                    ax.arrow((x + 0.5) * (width/boxes_x), (y + 0.5) * (height/boxes_y),
                         coords[0], coords[1],
                         head_width=12, head_length=12, fc='lime', ec='lime')
                elif ((angle < 0 or angle > math.pi) and x >= boxes_x/2):
                    ax.arrow((x + 0.5) * (width/boxes_x), (y + 0.5) * (height/boxes_y),
                         coords[0], coords[1],
                         head_width=12, head_length=12, fc='lime', ec='lime')
                else:
                    ax.arrow((x + 0.5) * (width/boxes_x), (y + 0.5) * (height/boxes_y),
                    coords[0], coords[1],
                    head_width=12, head_length=12, fc='black', ec='black')
                    displacement = -displacement  
                     
                color = sm.to_rgba(top_cells)
                if (count-top_cells != 0):
                    avg_normal_x = avg_normal_x/(count-top_cells)
                    avg_normal_y = avg_normal_y/(count-top_cells)
                else:
                    avg_normal_x = 0
                    avg_normal_y = 0
                if (top_cells != 0):
                    avg_fastest_x = avg_fastest_x/top_cells
                    avg_fastest_y = avg_fastest_y/top_cells
                else:
                    avg_fastest_x = 0
                    avg_fastest_y = 0

                box_boundary_distance = 0.0
                try:
                    crossings = np.array([])
                    box_center_y = (int)((height/boxes_y) * (y+0.5))
                    box_center_x = (int)((width/boxes_x) * (x+0.5))
                    for ls_x in range(0,width-1):
                        if (difSigns(phi[box_center_y, ls_x ],phi[box_center_y, ls_x + 1])):
                            #print(str(ls_x) + ", " + str(posy_int))
                            crossings = np.append(crossings, int(ls_x))
                    difference_array = np.absolute(crossings-box_center_x)
                    nearest_index = difference_array.argmin()
                    box_boundary_distance = difference_array[nearest_index]
                except Exception as e:
                    print(e)

                csv_data =  ("%d,%d,%0.3f,%0.3f,%d,%0.3f,%0.3f,%0.3f,%0.3f,%0.3f,%0.3f,%0.3f,%0.3f,%0.3f" % (x,y,box_center_x,box_center_y,avg_fastest_x,avg_fastest_y,top_cells,avg_x/count,avg_y/count,count,avg_normal_x,avg_normal_y,angle,box_boundary_distance))
                csv_file.write(csv_data + '\n')   
                csv_file.flush()
                
                #print(csv_data)
                sys.stdout.write('\rFrame %i region %i/%i' % (frame,x*boxes_y+y,boxes_x*boxes_y))
                rectangle = patches.Rectangle((x * (width/boxes_x), y * (height/boxes_y)), width/boxes_x, height/boxes_y, alpha=0.5, color = color)
                ax.add_patch(rectangle)
    
        image = mpimg.imread(replace_frame_digits(image_names, frame+1))
        ax.imshow(image,alpha=0.3)
        px_to_microns = 1.64787581699
        measure_label = '100 µm'
        measure_length_pixels = 100 / px_to_microns  # Adjust according to your image
        ob = AnchoredHScaleBar(size=measure_length_pixels, label=measure_label, loc=4, frameon=True, pad=0.6,sep=4, linekw=dict(color="black")) 
        ax.add_artist(ob)
    
        # Disable ticks and numbers on X and Y axes
        ax.tick_params(axis='x', which='both', bottom=False, top=False, labelbottom=False)
        ax.tick_params(axis='y', which='both', left=False, right=False, labelleft=False) 

        top_df.to_csv(replace_frame_digits(output_names, frame).replace(output_names[len(output_names) - 4:], '-fastest100.csv'), index=False)

        # LevelSet display
        contours = measure.find_contours(phi, 0)
        areas = []
        cv_contours = []
        x_coords = []

        for n, contour in enumerate(contours):
            c = np.expand_dims(contour.astype(np.float32), 1)
            c = cv2.UMat(c)
            cv_contours.append(c)
            #areas.append(abs(cv2.contourArea(c)))
            areas.append(cv2.arcLength(c,True))
            
        max_index_1 = np.argsort(areas)[-1]
        max_index_2 = np.argsort(areas)[-2]

        max_contour_1 = contours[max_index_1]
        max_contour_2 = contours[max_index_2]

        if (max_contour_1[0][1] < max_contour_2[0][1]):
            tmp = max_contour_2
            max_contour_2 = max_contour_1
            max_contour_1 = tmp

        ax.plot(max_contour_1[:, 1], max_contour_1[:, 0], linewidth=2, color='1.0')
        ax.plot(max_contour_2[:, 1], max_contour_2[:, 0], linewidth=2, color='1.0')
        ax.spines['top'].set_visible(False)
        ax.spines['right'].set_visible(False)
        ax.spines['bottom'].set_visible(False)
        ax.spines['left'].set_visible(False)

        sys.stdout.write('\n')
        print("Final plot saved to " + replace_frame_digits(output_names, frame))
        plt.savefig(replace_frame_digits(output_names, frame),dpi=300)
            
    return



def show_max_directions(df_spots, df_tracks, phi, image, output):
    plt.clf()
    df_tracks_sorted = df_tracks.sort_values(by='TOTAL_DISTANCE_TRAVELED', ascending=False)
    height, width = image.shape

    for id in df_tracks['TRACK_ID']:
        try:
            track = df_spots[df_spots['TRACK_ID'] == id]
            sposx_int = track['POSITION_X'].astype(int).iloc[0] #first
            sposy_int = track['POSITION_Y'].astype(int).iloc[0] #first
            eposx_int = track['POSITION_X'].astype(int).iloc[-1] #last
            eposy_int = track['POSITION_Y'].astype(int).iloc[-1] #last
            df_tracks['XDIFF'] = abs(sposx_int - eposx_int)
        except:
            df_tracks['XDIFF'] = 0


    #df_tracks_sorted = df_tracks.sort_values(by='TOTAL_DISTANCE_TRAVELED', ascending=False)
    #df_tracks_sorted = df_tracks.sort_values(by='XDIFF', ascending=False)
    df_tracks_sorted = df_tracks.sort_values(by='TRACK_DISPLACEMENT', ascending=False)

    top_100 = df_tracks_sorted.head(100)
    top_tracks = df_tracks_sorted;

    unique_ids = top_tracks['TRACK_ID'].unique()

    color_palette = plt.cm.get_cmap('tab10', len(unique_ids))
    fig, ax = plt.subplots()
    color = 0
    count = 0
    
    boxes_x = 20 #20
    boxes_y = 24 #24
      
    color_palette = plt.cm.get_cmap('tab10', boxes_x*boxes_y)
    cmap = plt.cm.get_cmap('RdYlGn') #Greens
    norm = colors.Normalize(vmin=-5, vmax=5)  # Normalize the values
    sm = plt.cm.ScalarMappable(norm=norm, cmap=cmap)  # Create a ScalarMappable object

    for x in range(0, boxes_x):
        for y in range(0, boxes_y):
            count = 1
            displacement = 0.0
            top_cells = 0
            avg_x = 0.0
            avg_y = 0.0
            for id in unique_ids:
                try:
                    track = df_spots[df_spots['TRACK_ID'] == id]
                    posx_int = track['POSITION_X'].astype(int).iloc[0] #start
                    posy_int = track['POSITION_Y'].astype(int).iloc[0] #start
                    if (posx_int > ((width/boxes_x) * x) and posx_int < ((width/boxes_x) * (x + 1))):
                        if (posy_int > ((height/boxes_y) * y) and posy_int < ((width/boxes_y) * (y + 1))):
                            ax.plot(posx_int, posy_int, marker='o', color = '0.3', alpha=0.3, markersize=1) 
                            count = count + 1
                            displacement = displacement + ((top_tracks[top_tracks['TRACK_ID'] == id]['TRACK_MEAN_SPEED']).astype(float).iloc[0])
                            avg_x = avg_x + (track['POSITION_X'].astype(int).iloc[-1]-track['POSITION_X'].astype(int).iloc[0])
                            avg_y = avg_y + (track['POSITION_Y'].astype(int).iloc[-1]-track['POSITION_Y'].astype(int).iloc[0])
                            if (id in top_100['TRACK_ID']):
                                top_cells = top_cells + 1
                except:
                    pass
            coords = normalize_vector(np.array([avg_x/count , avg_y/count]), 12)
            angle = math.atan2(coords[0], coords[1])
            if (angle > 0 and angle < math.pi and x < boxes_x/2):
                ax.arrow((x + 0.5) * (width/boxes_x), (y + 0.5) * (height/boxes_y),
                     coords[0], coords[1],
                     head_width=12, head_length=12, fc='lime', ec='lime')
            elif ((angle < 0 or angle > math.pi) and x >= boxes_x/2):
                ax.arrow((x + 0.5) * (width/boxes_x), (y + 0.5) * (height/boxes_y),
                     coords[0], coords[1],
                     head_width=12, head_length=12, fc='lime', ec='lime')
            else:
                ax.arrow((x + 0.5) * (width/boxes_x), (y + 0.5) * (height/boxes_y),
                 coords[0], coords[1],
                 head_width=12, head_length=12, fc='black', ec='black')
                displacement = -displacement  
                 
            #color = cmap(displacement/count)            
            #color = sm.to_rgba(displacement)
            color = sm.to_rgba(top_cells)
            print(top_cells)
            rectangle = patches.Rectangle((x * (width/boxes_x), y * (height/boxes_y)), width/boxes_x, height/boxes_y, alpha=0.5, color = color)
            ax.add_patch(rectangle)
                            
    ax.imshow(image,alpha=0.3)
    px_to_microns = 1.64787581699
    measure_label = '100 µm'
    measure_length_pixels = 100 / px_to_microns  # Adjust according to your image
    ob = AnchoredHScaleBar(size=measure_length_pixels, label=measure_label, loc=4, frameon=True, pad=0.6,sep=4, linekw=dict(color="black")) 
    ax.add_artist(ob)

    # Disable ticks and numbers on X and Y axes
    ax.tick_params(axis='x', which='both', bottom=False, top=False, labelbottom=False)
    ax.tick_params(axis='y', which='both', left=False, right=False, labelleft=False) 
   
    plt.savefig(output,dpi=300)

def track_cells(df_spots, df_tracks, phi, image, output):
    plt.clf()
    df_tracks_sorted = df_tracks.sort_values(by='TOTAL_DISTANCE_TRAVELED', ascending=False)
    height, width = image.shape

    for id in df_tracks['TRACK_ID']:
        try:
            track = df_spots[df_spots['TRACK_ID'] == id]
            sposx_int = track['POSITION_X'].astype(int).iloc[0] #first
            sposy_int = track['POSITION_Y'].astype(int).iloc[0] #first
            eposx_int = track['POSITION_X'].astype(int).iloc[-1] #last
            eposy_int = track['POSITION_Y'].astype(int).iloc[-1] #last
            df_tracks['XDIFF'] = abs(sposx_int - eposx_int)
        except:
            df_tracks['XDIFF'] = 0


    #df_tracks_sorted = df_tracks.sort_values(by='TOTAL_DISTANCE_TRAVELED', ascending=False)
    #df_tracks_sorted = df_tracks.sort_values(by='XDIFF', ascending=False)
    df_tracks_sorted = df_tracks.sort_values(by='TRACK_DISPLACEMENT', ascending=False)

    #top_100 = df_tracks_sorted.head(100)
    top_tracks = df_tracks_sorted;

    unique_ids = top_tracks['TRACK_ID'].unique()

    color_palette = plt.cm.get_cmap('tab10', len(unique_ids))
    fig, ax = plt.subplots()
    color = 0
    count = 0
    for id in unique_ids:
        try:
            track = df_spots[df_spots['TRACK_ID'] == id]
            posx_int = track['POSITION_X'].astype(int).iloc[-1] #last
            posy_int = track['POSITION_Y'].astype(int).iloc[-1] #last
            
            if (phi[track['POSITION_Y'].astype(int).iloc[0],track['POSITION_X'].astype(int).iloc[0]] > 0):
                count = count + 1
            
            #ax_start.plot(track['POSITION_X'], track['POSITION_Y'], marker='o', color=color_palette(color), label=f'Track {id}',markersize=3)
            if (phi[posy_int,posx_int] < 0):
                ax.plot(track['POSITION_X'], track['POSITION_Y'], marker='o', color='0.4', alpha=0.5, label=f'Track {id}',markersize=1)
            else:
                ax.plot(track['POSITION_X'], track['POSITION_Y'], marker='o', color=color_palette(color), alpha=0.5, label=f'Track {id}',markersize=2)
            color = color + 1
        except:
            #print("err")
            pass

    # phi contours
    #contours = measure.find_contours(phi, 0)
    #max_contour_1 = contours[0]
    #max_contour_2 = contours[1]
    #ax.plot(max_contour_1[:, 1], max_contour_1[:, 0], linewidth=2, color='1.0')
    #ax.plot(max_contour_2[:, 1], max_contour_2[:, 0], linewidth=2, color='1.0')

    contours = measure.find_contours(phi, 0)
    areas = []
    cv_contours = []
    x_coords = []


    for n, contour in enumerate(contours):
        c = np.expand_dims(contour.astype(np.float32), 1)
        c = cv2.UMat(c)
        cv_contours.append(c)
        #areas.append(abs(cv2.contourArea(c)))
        areas.append(cv2.arcLength(c,True))
            
    max_index_1 = np.argsort(areas)[-1]
    max_index_2 = np.argsort(areas)[-2]

    max_contour_1 = contours[max_index_1]
    max_contour_2 = contours[max_index_2]

    if (max_contour_1[0][1] < max_contour_2[0][1]):
        tmp = max_contour_2
        max_contour_2 = max_contour_1
        max_contour_1 = tmp


    x_coords = []    
    x_coords.extend(max_contour_1[:, 1])  # The x-coordinates are stored in the second column of the contour array
    average_x1 = np.mean(x_coords)
    #print(average_x1)

    x_coords = []    
    x_coords.extend(max_contour_2[:, 1])  # The x-coordinates are stored in the second column of the contour array
    average_x2 = np.mean(x_coords)
    #print(average_x2)

    data = [calc_contour_avg(max_contour_1, height), calc_contour_avg(max_contour_2, height), [average_x1],[average_x2],[count]]

    # Write the data to the CSV file
    with open("stats.txt", "a", newline="") as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(data)

    
    ax.plot(max_contour_1[:, 1], max_contour_1[:, 0], linewidth=2, color='1.0')
    ax.plot(max_contour_2[:, 1], max_contour_2[:, 0], linewidth=2, color='1.0')

    #ax.set_xlabel('X')
    #ax.set_ylabel('Y')
    #ax.set_title('Tracks of Points')
    #ax.legend(loc='center left',bbox_to_anchor=(1, 0.5),fontsize=6)
    
    px_to_microns = 1.64787581699
    measure_label = '100 µm'
    measure_length_pixels = 100 / px_to_microns  # Adjust according to your image
    ob = AnchoredHScaleBar(size=measure_length_pixels, label=measure_label, loc=4, frameon=True, pad=0.6,sep=4, linekw=dict(color="black")) 
    ax.add_artist(ob)

    # Disable ticks and numbers on X and Y axes
    ax.tick_params(axis='x', which='both', bottom=False, top=False, labelbottom=False)
    ax.tick_params(axis='y', which='both', left=False, right=False, labelleft=False)

    #plt.show(block=True)
    plt.savefig(output,dpi=300)

    
def add_progress_to_image(phi,image,iteration):
    height, width, channels = image.shape
    
    #colors_list = ['lime', 'yellow', 'red']
    colors_list = ['red', 'yellow', 'lime']
      
    cmap = colors.LinearSegmentedColormap.from_list('green_to_red', colors_list)

    # Generate a sample gradient
    gradient = np.linspace(0, 1, 100)

    # Convert gradient values to RGB colors
    rgba_values = cmap(gradient)
    rgb_values = rgba_values[:, :3]
    
    for y in range(0,height):
        for x in range(0,width):
            red, green, blue = image[y,x]
            if (phi[y,x] > 0):
                image[y,x] = rgb_values[iteration] 
 
    return image



