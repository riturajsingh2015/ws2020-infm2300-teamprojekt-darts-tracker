# -*- coding: utf-8 -*-
"""
Created on Tue Jan 19 08:57:15 2021

@author: avoigt
"""

import cv2
import numpy as np
import imutils
import matplotlib.pyplot as plt

img = cv2.imread('20210119_100740.jpg')

hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

# find yellow spots
lowYellow = (19, 75, 91);
upYellow = (50, 255, 255);

yellowSpots = cv2.inRange(hsv,lowYellow,upYellow); ## output CV_8U, like binary image

# remove more noise, erode and dilate could help or even blur
# Taking a matrix of size 5 as the kernel 
#kernel = np.ones((2,2), np.uint8) 
#yellowSpots_erode = cv2.erode(yellowSpots, kernel, iterations=1)

contours, hierarchy = cv2.findContours(yellowSpots,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
#contours = imutils.grab_contours(contours)

# loop over the contours
for c in contours:
    if cv2.contourArea(c) > 20:
        # compute the center of the contour
        M = cv2.moments(c)
        cX = int(M["m10"] / M["m00"])
        cY = int(M["m01"] / M["m00"])
        # draw the contour and center of the shape on the image
        cv2.drawContours(yellowSpots, [c], -1, (0, 255, 0), 2)
        print((cX, cY))
        cv2.circle(img, (cX, cY), 7, (255, 255, 255), -1)
        cv2.putText(img, "Point", (cX - 20, cY - 20), cv2.FONT_HERSHEY_SIMPLEX, 1.5, (255, 255, 255), 4)
        
# show the image
#cv2.imshow("Image", img)
#cv2.waitKey(0)
cv2.imwrite("output.jpg", img)
 
##cv2.waitKey(0)
#plt.imshow(yellowSpots,cmap='gray')
#cv2.imshow(yellowSpots);


#rows,cols,ch = img.shape
#pts1 = np.float32([[2640,627],[1942,1416],[3408,1380],[2646,2435]])
#pts2 = np.float32([[750,220],[220,750],[1480,750],[750,1480]])
#M = cv2.getPerspectiveTransform(pts1,pts2)
#dst = cv2.warpPerspective(img,M,(5300,3000))
#plt.subplot(121),plt.imshow(img),plt.title('Input')
#crop_img = dst[0:1701, 0:1701]
#plt.subplot(122),plt.imshow(crop_img),plt.title('Output')
#plt.show()