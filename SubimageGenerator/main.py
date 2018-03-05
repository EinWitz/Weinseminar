import os
import fnmatch
import skvideo.io
import skimage.io
import skimage.transform
import subprocess
import math
import shutil

rootPath = "/Users/kjs/Desktop/Daten/"
destPath = "/Volumes/Extern"
seats = 5
digitNum = 15

#[[width, height, col_step_size, row_step_size, rows],[width2, height2, col_step_size2, row_step_size2, rows2]]
dimensions = [
    [150, 300, 150, 300, 1]
]

#Set use frame & skip frame toggle seconds; Starting with use frame
toggle = {
    "GOPR7750.MP4": [0, 3, 79],
    "GOPR9111.MP4": [0, 3, 50],
    "GOPR8445.MP4": [0, 55, 95],
    "GOPR0463.MP4": [0, 7, 55],
    "GOPR1175.MP4": [0, 5, 89],
    "GOPR1148.MP4": [0, 3, 93],
    "GOPR1857.MP4": [0, 4, 135],
    "GOPR2040.MP4": [0, 2, 44],
    "GOPR5789.MP4": [0, 4, 125, 136, 162],
    "GOPR6941.MP4": [0, 2, 56],
    "GOPR6251.MP4": [0, 2, 58],
    "GOPR1850.MP4": [0],
    "GOPR1856.MP4": [0],
    "GOPR1885.MP4": [0],
    "GOPR1886.MP4": [0],
    "GOPR1887.MP4": [0],
    "GOPR1888.MP4": [0],
    "GOPR1893.MP4": [0],
    "GOPR1894.MP4": [0, 3, 28],
    "GOPR1912.MP4": [0, 13, 54, 60, 103, 155, 202, 252, 297],
    "GOPR2941.MP4": [0, 7, 18, 29, 35],
    "GOPR8358.MP4": [0, 4, 72, 78, 149],
    "GOPR9465.MP4": [0, 48, 92],
    "GOPR8783.MP4": [0, 3, 42, 50, 89],
    "GOPR0174.MP4": [0, 6, 90, 99, 174],
    "GOPR0236.MP4": [0, 4, 49, 58, 104],
    "GOPR0211.MP4": [0, 5, 53, 64, 114],
    "GOPR0183.MP4": [0, 3, 26],
    "GOPR0184.MP4": [0, 1, 22, 28, 49, 83, 129, 136, 183, 190, 236, 244, 292, 300, 346, 368, 415, 423, 471, 478, 527, 535, 624],
    "GOPR0185.MP4": [0, 10, 28, 66, 104, 110, 145, 171, 340],
    "GOPR0186.MP4": [0, 2, 26, 78, 153, 174, 300, 383, 624],
    "GOPR0187.MP4": [0, 26, 32, 37, 43, 70, 89, 111, 122],
    "GOPR0188.MP4": [0, 1, 10, 25, 55, 80, 112, 127, 195, 223, 250, 278, 535],
    "GOPR0189.MP4": [0, 4, 57, 75, 129, 150, 201],
    "GP010186.MP4": [0, 0, 11, 18, 28, 34, 47, 210, 224, 226, 268, 280, 284, 479, 498, 519, 554, 567, 592, 603, 624],
    "GP020186.MP4": [0, 0, 7, 35, 62, 71, 118],
    "GP010188.MP4": [0, 26, 47, 84, 100, 128, 143, 311, 326, 333, 335, 354, 357, 378, 400, 451, 473, 517, 531, 576, 589],
    "GP010189.MP4": [0, 0, 23, 51, 104, 124, 171, 203, 248, 265, 304, 334, 369, 385, 418, 441, 471, 486, 509, 535, 557],
    "GP020189.MP4": [0],
    "GP030189.MP4": [0],
    "GOPR2019.MP4": [0, 4, 73, 82, 152],
    "GOPR3139.MP4": [0, 4, 41, 49, 97],
    "GOPR2472.MP4": [0, 4, 49, 56, 121],
    "GOPR3166.MP4": [0, 5, 69, 77, 151],
    "GOPR4319.MP4": [0, 2, 43, 50, 95],
    "GOPR4289.MP4": [0, 4, 51, 58, 107],
    "GOPR5670.MP4": [0, 6, 81, 89, 173],
    "GOPR7011.MP4": [0, 4, 45, 54, 100],
    "GOPR6348.MP4": [0, 3, 45, 52, 105],
}

rotation = {
    "GOPR1857.MP4": 2
}

# END CONFIG

# Iterate recusively through rootPath and get all MP4 files
matches = []
for root, dirnames, filenames in os.walk(rootPath):
    for filename in fnmatch.filter(filenames, '*.MP4'):
        matches.append(os.path.join(root, filename))

# Create dimension folders
for dim in dimensions:
    if not os.path.exists(destPath + "/" + str(dim[0]) + "x" + str(dim[1]) + "_" + str(dim[2]) + "_" + str(dim[3])):
        os.makedirs(destPath + "/" + str(dim[0]) + "x" + str(dim[1]) + "_" + str(dim[2]) + "_" + str(dim[3]))
    else:
        print("Dimension folder already exists. This will cause problems with the distribution to seats")
        exit(-1)

# skip duplicates
done = []

# Iterate over all MP4 files
for vid in matches:
    if os.path.basename(vid) not in done:
        done.append(os.path.basename(vid))
        vidcap = skvideo.io.vreader(vid)
        print("Opened video " + vid)

        # Iterate over all frames of this file
        fn = 0          # frame number
        skip = False    # skip state
        tp = []         # toggle points for current video
        ntp = 0         # next toggle point
        rotate = False

        if os.path.basename(vid) in toggle:
            tp = toggle[os.path.basename(vid)]

        if os.path.basename(vid) in rotation:
            rotate = True

        for frame in vidcap:
            if ntp < len(tp):
                if fn % 25 == 0 and fn / 25 == tp[ntp]:
                    skip = not skip
                    ntp += 1

            if not skip:
                print("Opened Frame " + str(fn))

                if rotate:
                    ang = 0
                    if rotation[os.path.basename(vid)] == 0:
                        ang = -90
                    elif rotation[os.path.basename(vid)] == 1:
                        ang = 180
                    elif rotation[os.path.basename(vid)] == 2:
                        ang = 90
                    frame = skimage.transform.rotate(frame, ang, True)

                # read shape
                frows, fcols, _ = frame.shape

                # Foreach dimension
                for dim in dimensions:
                    print("Dimension: " + str(dim[0]) + "x" + str(dim[1]) + "_" + str(dim[2]) + "_" + str(dim[3]))
                    width = dim[0]
                    height = dim[1]
                    css = dim[2]
                    rss = dim[3]

                    y = frows - height
                    rn = 0

                    # foreach row, starting from the bottom of the frame
                    while height > 0 and rn < dim[4]:
                        print("Row " + str(rn + 1) + " (max: " + str(dim[4]) + ")")
                        x = 0
                        cn = 0

                        # foreach column, starting on the left of the frame
                        while x + width <= fcols:
                            skimage.io.imsave(destPath + "/" + str(width) + "x" + str(height) + "_" + str(css) + "_" + str(rss) + "/" + os.path.basename(vid) + "_" + str(fn) + "_" + str(rn) + "_" + str(cn) + ".jpg", frame[y:y + height, x:x + width])

                            with open(destPath + "/" + str(width) + "x" + str(height) + "_" + str(css) + "_" + str(rss) + "/files.txt", "a") as log:
                                log.write(destPath + "/" + str(width) + "x" + str(height) + "_" + str(css) + "_" + str(rss) + "/" + os.path.basename(vid) + "_" + str(fn) + "_" + str(rn) + "_" + str(cn) + ".jpg\n")

                            cn += 1
                            x += css

                        print(str(cn) + " columns")
                        rn += 1
                        y -= rss

            fn += 1


# shuffle and distribute exported images to seats
print("Start with shuffeling")
for dim in dimensions:

    #create folders
    for s in range(seats):
        if not os.path.exists(destPath + "/" + str(dim[0]) + "x" + str(dim[1]) + "_" + str(dim[2]) + "_" + str(dim[3]) + "/seat_" + str(s)):
            os.makedirs(destPath + "/" + str(dim[0]) + "x" + str(dim[1]) + "_" + str(dim[2]) + "_" + str(dim[3]) + "/seat_" + str(s))

    # shuffle image file
    sortPath = destPath + "/" + str(dim[0]) + "x" + str(dim[1]) + "_" + str(dim[2]) + "_" + str(dim[3]) + "/files.txt"
    shufflePath = destPath + "/" + str(dim[0]) + "x" + str(dim[1]) + "_" + str(dim[2]) + "_" + str(dim[3]) + "/shuffled.txt"
    command = "sort -R " + sortPath + " > " + shufflePath
    output = subprocess.check_output(['bash', '-c', command])
    print("Sort Output: " + str(output))

    # move images
    with open(shufflePath) as f:
        c = 0
        for line in f:
            line = line.rstrip()
            num = str(math.floor(c / seats))
            while len(num) < digitNum:
                num = "0" + num

            dest = destPath + "/" + str(dim[0]) + "x" + str(dim[1]) + "_" + str(dim[2]) + "_" + str(dim[3]) + "/seat_" + str(c % seats) + "/" + num + "_S" + str(c % seats) + "_" + os.path.basename(line)

            print("move " + line + " to " + dest)
            shutil.move(line, dest)
            c += 1