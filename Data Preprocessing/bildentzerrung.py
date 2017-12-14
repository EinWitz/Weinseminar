# -*- coding: utf-8 -*-
"""
Created on Wed Dec 13 16:26:09 2017

@author: Philipp
"""

import cv2
import numpy as np
import math

image = cv2.imread("bild2.jpg")
image2 = cv2.imread("bild2.jpg")
#image = cv2.resize(image, (960, 540))  
#image2 = cv2.resize(image, (960, 540))  

gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

y=image.shape[1]
x=image.shape[0]


halfWidth = x/2.0
halfHeight = y/2.0

strength = 0.02

correctionRadius = math.sqrt(x^2+y^2)/strength

for j in range (0,x):
    for  i in range (0,y):
        
        
        newX = j-halfWidth
        newY = i-halfHeight
        
        distance = math.sqrt(newX**2 + newY**2)
        r = distance / correctionRadius
        
        if r==0:
            theta = 1
        else:
            theta = np.arctan(r)/r
            
        sourceX = int(halfWidth + theta * newX)
        sourceY = int(halfHeight + theta * newY)
        
        
        image2[j,i] = image[sourceX,sourceY]
        
        
cv2.imwrite("image2.jpg",image2)
