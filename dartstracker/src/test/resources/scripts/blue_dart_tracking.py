import cv2 as cv
import math

## Blue color filter
lowBlue = (85, 60, 45);
upBlue = (130, 255, 255);
## Yellow spots
lowYellow = (25, 75, 91);
upYellow = (35, 255, 255);


## Using a video as input
file = "../testInput/bordA/20210531_170354.mp4"
capture = cv.VideoCapture(cv.samples.findFileOrKeep(file))
if not capture.isOpened():
    print('Unable to open: ' + file)
    exit(0)

## Analyze every single frame an show
while True:
    ret, frame = capture.read()
    if frame is None:
        break

    ## [color segementation]
    hsv = cv.cvtColor(frame, cv.COLOR_BGR2HSV)
    blueMask = cv.inRange(hsv,lowBlue,upBlue); ## output CV_8U, like binary image
    
    ## [display frame number]
    cv.rectangle(frame, (10, 2), (100,20), (255,255,255), -1)
    cv.putText(frame, str(capture.get(cv.CAP_PROP_POS_FRAMES)), (15, 15), cv.FONT_HERSHEY_SIMPLEX, 0.5 , (0,0,0))

    ## Finde die Größte Pixelmasse und markiere sie auf dem orginalframe
    contours, hierarchy = cv.findContours(blueMask, cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE)
    for c in contours:
        if cv.contourArea(c) > 400: 
            cv.drawContours(frame, [c], -1, (0, 255, 255), 1)
            
            # find Dartspitze und markiere sie
            M = cv.moments(c)
            cX = int(M["m10"] / M["m00"])
            cY = int(M["m01"] / M["m00"])
            massCentroid = (cX, cY)
            dart_tip = massCentroid
            # cv.circle(frame, massCentroid, 3, (255, 0, 0), 2)
            distance = 0
            for cc in c:
                contourPoint = (cc[0][0], cc[0][1])
                tempDisctance = math.dist(massCentroid, contourPoint)
                if tempDisctance > distance:
                    distance = tempDisctance
                    dart_tip = contourPoint
                    
            cv.circle(frame, dart_tip, 3, (0, 0, 255), 2)
            break;
    
    ## Finde gelbe Hilfspunkte
    yellowSpots = cv.inRange(hsv,lowYellow,upYellow);
    contours, hierarchy = cv.findContours(yellowSpots,cv.RETR_TREE,cv.CHAIN_APPROX_SIMPLE)
    for c in contours:
        if cv.contourArea(c) > 2:
            # compute the center of the contour
            M = cv.moments(c)
            cX = int(M["m10"] / M["m00"])
            cY = int(M["m01"] / M["m00"])
            # draw the contour and center of the shape on the image
            cv.drawContours(yellowSpots, [c], -1, (0, 255, 0), 2)
            cv.circle(frame, (cX, cY), 7, (255, 255, 255), -1)
    
    ## [show actual video frame and blue mask]
    cv.imshow('Frame', frame)
    cv.imshow('Blue Mask', blueMask)
    ## [show]

    # check frame and collect biggest contour area
    if cv.waitKey(10) & 0xFF == ord('q'):
        break

