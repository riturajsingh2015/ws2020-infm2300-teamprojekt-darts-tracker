from __future__ import print_function
import cv2 as cv


## [create]
#create Background Subtractor objects
# https://docs.opencv.org/3.4/de/de1/group__video__motion.html
#backSub = cv.createBackgroundSubtractorMOG2()
#backSub = cv.createBackgroundSubtractorMOG2(300,500,True)
#backSub = cv.createBackgroundSubtractorMOG2(60,50,False)
#backSub = cv.createBackgroundSubtractorKNN()
backSub = cv.createBackgroundSubtractorKNN(history=60, dist2Threshold=300.0, detectShadows=True)
# there are more background subtractors with: pip install opencv-contrib-python
# https://docs.opencv.org/master/d2/d55/group__bgsegm.html
#backSub = cv.bgsegm.createBackgroundSubtractorGMG()
#backSub = cv.bgsegm.createBackgroundSubtractorGSOC()
#backSub = cv.bgsegm.createBackgroundSubtractorLSBP()
#backSub = cv.bgsegm.createBackgroundSubtractorCNT(minPixelStability=10, maxPixelStability=300)
## [create]

## [capture]
file = "../testInput/bordA/20210520_075612.mp4"
capture = cv.VideoCapture(cv.samples.findFileOrKeep(file))
if not capture.isOpened():
    print('Unable to open: ' + file)
    exit(0)
## [capture]

fgMask = None
while True:
    ret, frame = capture.read()
    if frame is None:
        break

    # Bewegungen des Boards sind schwierig - shuttering - only take every n-th frame
#    if capture.get(cv.CAP_PROP_POS_FRAMES) % 30 != 0:
#        continue;
    
    ## [apply]
    #update the background model
#    fgMask = backSub.apply(frame) # ohne lernrate, ohne fgMask update
    fgMask = backSub.apply(frame, learningRate=0.9)
#    fgMask = backSub.apply(frame, fgMask, learningRate=0.2) #
    ## [apply]

    ## [display_frame_number]
    #get the frame number and write it on the current frame
    cv.rectangle(frame, (10, 2), (100,20), (255,255,255), -1)
    cv.putText(frame, str(capture.get(cv.CAP_PROP_POS_FRAMES)), (15, 15), cv.FONT_HERSHEY_SIMPLEX, 0.5 , (0,0,0))
    ## [display_frame_number]

    ## [show]
    #show the current frame and the fg masks
    cv.imshow('Frame', frame)
    cv.imshow('FG Mask', fgMask)
    ## [show]

    # check frame and collect biggest contour area
    
    keyboard = cv.waitKey(30)
    if keyboard == 'q' or keyboard == 27:
        break