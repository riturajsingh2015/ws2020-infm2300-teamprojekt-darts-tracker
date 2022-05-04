# -*- coding: utf-8 -*-
"""
Created on Tue Jan  5 15:03:17 2021
https://docs.opencv.org/master/da/d6e/tutorial_py_geometric_transformations.html

https://docs.opencv.org/3.4/da/d97/tutorial_threshold_inRange.html
https://www.google.com/search?q=sift+features+opencv+python&oq=sift+features+opencv+&aqs=chrome.1.69i57j0i457j0i22i30l6.11503j0j7&sourceid=chrome&ie=UTF-8
https://www.youtube.com/watch?v=Fe-KWKPk9Zc
https://www.youtube.com/watch?v=USl5BHFq2H4
https://pysource.com/2018/03/21/feature-detection-sift-surf-obr-opencv-3-4-with-python-3-tutorial-25/
@author: avoigt
"""
import cv2
import numpy as np
import matplotlib.pyplot as plt

img = cv2.imread('test.jpg')
rows,cols,ch = img.shape
pts1 = np.float32([[2640,627],[1942,1416],[3408,1380],[2646,2435]])
pts2 = np.float32([[750,220],[220,750],[1480,750],[750,1480]])
M = cv2.getPerspectiveTransform(pts1,pts2)
dst = cv2.warpPerspective(img,M,(5300,3000))
plt.subplot(121),plt.imshow(img),plt.title('Input')
crop_img = dst[0:1701, 0:1701]
plt.subplot(122),plt.imshow(crop_img),plt.title('Output')
plt.show()