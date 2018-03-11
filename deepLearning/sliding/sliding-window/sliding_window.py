# USAGE
# source: https://www.pyimagesearch.com/2015/03/23/sliding-windows-for-object-detection-with-python-and-opencv/
# python sliding_window.py --image images/imagename.jpg 
# also check the stepsize

# import the necessary packages
from pyimagesearch.helpers import pyramid
from pyimagesearch.helpers import sliding_window
import argparse
import time
import cv2
#added by me
from keras.models import load_model
import numpy as np
from keras.applications.imagenet_utils import preprocess_input

# import the trained model to use later (returns a compiled model)
model = load_model("C:/Users/JansPC/Dropbox/9. Semester/Weinseminar/Coursera/wineModel/best_wineModel_1119_255_2Layer.h5")

# construct the argument parser and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True, help="Path to the image")
args = vars(ap.parse_args())

# load the image and define the window width and height
image = cv2.imread(args["image"])
(winW, winH) = (112, 112)

# create empty list to store the predictions later
windows = []

# loop over the image pyramid
for resized in pyramid(image, scale=1.5):
	# loop over the sliding window for each layer of the pyramid
	for (x, y, window) in sliding_window(resized, stepSize=112, windowSize=(winW, winH)):
		# if the window does not meet our desired window size, ignore it
		if window.shape[0] != winH or window.shape[1] != winW:
			continue

		# THIS IS WHERE YOU WOULD PROCESS YOUR WINDOW, SUCH AS APPLYING A
		# MACHINE LEARNING CLASSIFIER TO CLASSIFY THE CONTENTS OF THE
		# WINDOW
		

		window = window.astype('float32')
		window = np.expand_dims(window, axis=0)
		window = preprocess_input(window)
	
		#print (window.shape)
		#print (model.predict(window))
		#if model.predict(window) == 1:	
		if True:
			prediction = model.predict(window)
			# since we do not have a classifier, we'll just draw the window
			clone = resized.copy()
			cv2.rectangle(clone, (x, y), (x + winW, y + winH), (0, 255, 0), 2)
			cv2.imshow("Window", clone)
			cv2.waitKey(3)
			time.sleep(0.015)
			windows.append(window)