# Smart-Shoe for blind
Introduction
IoT-based smart shoes for the blind are made using ultrasonic sensors connected to an raspberry pi board. IoT communicates with objects or people through physical objects. Technology has grown and grown in the market.
It is extremely difficult for a blind person to move independently. They should always be dependent on others in many areas of their lives. One of the biggest problems is that they have to walk the streets. They can't figure out every obstacle in the way with an ordinary stick. So these smart shoes offer them a long-term solution. Plus, it will help them reach their destination stress-free and independently. Built with IoT technology, the shoe has multiple sensors, microcontrollers, and buzzers embedded in it. If there is an obstacle in front of the user, in our project the shoe will sound a warning through a buzzer. the integrated cam detect objects andetify them and spell the name of object to blind person via android app  Smart shoes coordinate and communicate with each other to ensure that users do not collide with obstacles.


[![Everything Is AWESOME](https://img.youtube.com/vi/GEd3EM0iMgM-Y/0.jpg)](https://www.youtube.com/watch?v=GEd3EM0iMgM-Y "Everything Is AWESOME")
Getting Start
Before you start this project, you may need to prepare your hardware and software in advance as described here.

Hardware preparation:

rasberry pi 
PiCam
ultrasonic sensor
buzzer

Software Preparation
We recommend installing the Bullesye or Bookworm version of Raspberry Pi 64 bit OS from their official website. If you prefer to install a new Raspbian OS,
please follow the steps outlined in this guide https://www.raspberrypi.com/news/bookworm-the-new-version-of-raspberry-pi-os/.

NOTE We highly recommend checking out our previous tutorial on Getting started with OpenCV,  This guide is specifically for Bulleseye OS and Bookworm OS. Please take note of the installation dependencies, as there are two distinct methods outlined here.

TinyML TinyML revolutionizes machine learning by enabling lightweight models to run on edge devices with minimal resources. In the context of object detection, TensorFlow Lite, a key framework in TinyML, optimizes models for efficiency, allowing them to be deployed on devices like microcontrollers and embedded systems. This integration facilitates real-time, on-device processing for tasks such as object recognition, making TinyML with TensorFlow Lite ideal for applications in smart sensors, wearables, and IoT devices without the need for constant cloud connectivity.

EfficientDet EfficientDet is a highly efficient and accurate object detection model that stands out for its performance on edge devices. Developed by Google, EfficientDet optimizes the balance between model accuracy and computational efficiency, making it well-suited for deployment on resource-constrained environments such as edge devices and mobile platforms. It leverages a compound scaling method to efficiently scale up model parameters and achieve better accuracy without compromising speed. EfficientDet's architecture includes a feature network for capturing image features effectively and a compound scaling method for balancing model accuracy and computational efficiency. Its success lies in achieving impressive performance on various object detection benchmarks while maintaining a lightweight structure, making it a go-to choice for applications requiring real-time object detection on edge devices.

Let's run the code in Bullseye OS.
Make sure that you are in correct folder. If not
```
cd Seeed_Python_ReTerminal/samples/Opencv_and_piCam/ObjectDetection
```
Make sure to install the dependencies and the EfficientDet model. If you've already completed this step in our first tutorial, there's no need to worry.
```
sh setup.sh
```
Run the code
```
python3 detect_mod.py
```
Let's run the code in BookWorm OS.
Step 1 For that you need to create a Virtual environmnet.
NOTE
In earlier OS versions, Python libraries could be directly installed system-wide using pip, the Python package installer. However, in Bookworm and subsequent releases, a shift has occurred. To mitigate potential issues during installation, it is now necessary to install packages via pip into a Python virtual environment using venv.

Execute these commands one by one, and you will end up with a virtual environment.
```
mkdir my_project
cd my_project
python -m venv --system-site-packages env
source env/bin/activate
```
Step 2 Next, clone this Git repo onto your Raspberry Pi virtual environmnet like this
```
git clone https://github.com/Seeed-Studio/Seeed_Python_ReTerminal
```
Step 3 Next, utilize our script to effortlessly install the required Python packages and download the EfficientDet-Lite model. Navigate to this folder.
```
cd Seeed_Python_ReTerminal/samples/Opencv_and_piCam/ObjectDetection_bookworm
```
Step 3 The script install the required dependencies and download the TFLite models For this tutorial series.
```
sh setup.sh
```
copy this modified code and save it as laith.py at the same directory note that u must change the value of firebase into pyrbase
```python
import argparse
import sys
import time
import cv2
import mediapipe as mp
import pyrebase  # Import Pyrebase
import json  # Import JSON module for serialization
import numpy as np  # Import NumPy for array handling
from mediapipe.tasks import python
from mediapipe.tasks.python import vision
from utils import visualize
from picamera2 import Picamera2
import argparse
import sys
import time
import cv2
import mediapipe as mp
from mediapipe.tasks import python
from mediapipe.tasks.python import vision
from utils import visualize
from picamera2 import Picamera2

# Global variables to calculate FPS
COUNTER, FPS = 0, 0
START_TIME = time.time()
picam2 = Picamera2()
picam2.preview_configuration.main.size = (640,480)
picam2.preview_configuration.main.format = "RGB888"
picam2.preview_configuration.align()
picam2.configure("preview")
picam2.start()

# Initialize Pyrebase change ur own value here 
config = {
    "apiKey": " ",
    "authDomain": " ",
    "databaseURL": "     ",
    "projectId": "             ",
    "storageBucket": "                     ",
    "messagingSenderId": "               ",
    "appId": "                   ",
    "measurementId": "                "
}
firebase = pyrebase.initialize_app(config)
db = firebase.database()


def run(model: str, max_results: int, score_threshold: float, 
        camera_id: int, width: int, height: int) -> None:
  # Visualization parameters
  row_size = 50  # pixels
  left_margin = 24  # pixels
  text_color = (0, 0, 0)  # black
  font_size = 1
  font_thickness = 1
  fps_avg_frame_count = 10

  detection_frame = None
  detection_result_list = []

      

  def save_result(result: vision.ObjectDetectorResult, unused_output_image: mp.Image, timestamp_ms: int):
      global FPS, COUNTER, START_TIME

      # Calculate the FPS
      if COUNTER % fps_avg_frame_count == 0:
          FPS = fps_avg_frame_count / (time.time() - START_TIME)
          START_TIME = time.time()

      detection_result_list.append(result)
      COUNTER += 1
 

  # Initialize the object detection model
  base_options = python.BaseOptions(model_asset_path=model)
  options = vision.ObjectDetectorOptions(base_options=base_options,
                                         running_mode=vision.RunningMode.LIVE_STREAM,
                                         max_results=max_results, score_threshold=score_threshold,
                                         result_callback=save_result)
  detector = vision.ObjectDetector.create_from_options(options)


  # Continuously capture images from the camera and run inference
  while True:
    im= picam2.capture_array() 

 #   success, image = cap.read()
    image=cv2.resize(im,(640,480))
    image = cv2.flip(image, -1)
    image=cv2.rotate(image,cv2.ROTATE_180)

    # Convert the image from BGR to RGB as required by the TFLite model.
    rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb_image)
    #print(mp_image)

    # Run object detection using the model.
    detector.detect_async(mp_image, time.time_ns() // 1_000_000)
    # Show the FPS
    fps_text = 'FPS = {:.1f}'.format(FPS)
    text_location = (left_margin, row_size)

    current_frame = image
    cv2.putText(current_frame, fps_text, text_location, cv2.FONT_HERSHEY_DUPLEX,
                font_size, text_color, font_thickness, cv2.LINE_AA)
                
    if detection_result_list:
        first_element = detection_result_list[0]
        string_data = str(first_element)
        letter_set = set(string_data)
        #####print('Set of Letters:', letter_set)
        cleaned_string = string_data.replace(" ", "").replace("'", "")
        #####print(cleaned_string)
        
        first_element = detection_result_list[0]
        # Convert the data to a string
        string_data = str(first_element)
        # Remove spaces and quotation marks from the string
        cleaned_string = string_data.replace(" ", "").replace("'", "")
        index = cleaned_string.find('category_name=')

        if index != -1:
        # Extract the next 12 letters after 'category_name=' using string slicing
            next_12_letters = cleaned_string[index + len('category_name='):index + len('category_name=') + 12]
            next_12_letters_cleaned = next_12_letters.replace(')', '').replace(']', '').replace(',', '').replace('key', '')
            print('Object Detection=', next_12_letters_cleaned)
            db.child("node").push(next_12_letters_cleaned)

        else:
            print('No "Object" found in the string.')
  
        current_frame = visualize(current_frame, detection_result_list[0])
        detection_frame = current_frame
        detection_result_list = []
    

    if detection_frame is not None:
        cv2.imshow('object_detection', detection_frame)


    # Stop the program if the ESC key is pressed.
    if cv2.waitKey(1) == 27:
      break

  detector.close()
  cap.release()
  cv2.destroyAllWindows()


def main():

  parser = argparse.ArgumentParser(
      formatter_class=argparse.ArgumentDefaultsHelpFormatter)
  parser.add_argument(
      '--model',
      help='Path of the object detection model.',
      required=False,
      default='efficientdet_lite0.tflite')
      #default='best.tflite')
  parser.add_argument(
      '--maxResults',
      help='Max number of detection results.',
      required=False,
      default=5)
  parser.add_argument(
      '--scoreThreshold',
      help='The score threshold of detection results.',
      required=False,
      type=float,
      default=0.25)
  # Finding the camera ID can be very reliant on platform-dependent methods. 
  # One common approach is to use the fact that camera IDs are usually indexed sequentially by the OS, starting from 0. 
  # Here, we use OpenCV and create a VideoCapture object for each potential ID with 'cap = cv2.VideoCapture(i)'.
  # If 'cap' is None or not 'cap.isOpened()', it indicates the camera ID is not available.
  parser.add_argument(
      '--cameraId', help='Id of camera.', required=False, type=int, default=0)
  parser.add_argument(
      '--frameWidth',
      help='Width of frame to capture from camera.',
      required=False,
      type=int,
      default=640)
  parser.add_argument(
      '--frameHeight',
      help='Height of frame to capture from camera.',
      required=False,
      type=int,
      default=480)
  args = parser.parse_args()

  run(args.model, int(args.maxResults),
      args.scoreThreshold, int(args.cameraId), args.frameWidth, args.frameHeight)


  
if __name__ == '__main__':
  main()

```
Run the code
```
python3 laith.py
```

this code bellow calculate the real-time distance via ultrasonic sensor and send the value via firebase ver pyrbase,physical buzzer run too plz save this python code at ur rasp and run it at startup using this tutorial https://linuxconfig.org/how-to-autostart-python-script-on-raspberry-pi.
```
import RPi.GPIO as GPIO
import time
import pyrebase

# Firebase configuration (replace with your own credentials)
config = {
  "apiKey": "              ",
  "authDomain": "                      ",
  "databaseURL": "                            ",
  "projectId": "                      ",
  "storageBucket": "                ",
  "messagingSenderId": "                            ",
  "appId": "                               ",
  "measurementId": "                          "
}
firebase = pyrebase.initialize_app(config)
database = firebase.database()

# Buzzer setup
BUZZER_PIN = 5
GPIO.setmode(GPIO.BCM)
GPIO.setup(BUZZER_PIN, GPIO.OUT)

try:
    TRIG = 4
    ECHO = 17
    maxTime = 0.04
    
    firebase = pyrebase.initialize_app(config)
    database = firebase.database()
    while True:
        GPIO.setup(TRIG, GPIO.OUT)
        GPIO.setup(ECHO, GPIO.IN)

        GPIO.output(TRIG, False)
        time.sleep(0.01)

        GPIO.output(TRIG, True)
        time.sleep(0.00001)
        GPIO.output(TRIG, False)

        pulse_start = time.time()
        timeout = pulse_start + maxTime
        while GPIO.input(ECHO) == 0 and pulse_start < timeout:
            pulse_start = time.time()

        pulse_end = time.time()
        timeout = pulse_end + maxTime
        while GPIO.input(ECHO) == 1 and pulse_end < timeout:
            pulse_end = time.time()

        pulse_duration = pulse_end - pulse_start
        distance = pulse_duration * 17000
        distance = round(distance,2)
            
    
        # Update Firebase with distance data
        #f distance < 150:
        data = {"distance": distance}
        database.set(data)
        #database.child("child")
        #db.child("distance").push(distance)


        print(distance)
          

        # Check if distance is less than 30 and play buzzer
        if distance < 30:
            GPIO.output(BUZZER_PIN, GPIO.HIGH)
            time.sleep(0.5)  # Buzzer duration
            GPIO.output(BUZZER_PIN, GPIO.LOW)

except KeyboardInterrupt:
    GPIO.cleanup()
```


# Python Library for reTerminal

## Installation

### From PyPI

# smart-shoe
# smart-shoe
