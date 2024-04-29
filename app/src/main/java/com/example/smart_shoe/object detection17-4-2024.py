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

# Initialize Pyrebase
config = {
    "apiKey": "                 ",
    "authDomain": "                    ",
    "databaseURL": "                      ",
    "projectId": "                     ",
    "storageBucket": "                      ",
    "messagingSenderId": "                      ",
    "appId": "                      ",
    "measurementId": "                         "
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
            
            next_12_letters_cleaned = next_12_letters_cleaned.rstrip("poi")  # Removes "poi" from the end
            next_12_letters_cleaned = next_12_letters_cleaned.rstrip("po")   # Removes "po" from the end
            next_12_letters_cleaned = next_12_letters_cleaned.rstrip("p") 
            print('Object Detection=', next_12_letters_cleaned)
            data = {"name": next_12_letters_cleaned}
            db.child("object").set(data)
            #db.child("objectnode").push(next_12_letters_cleaned)
            
            #data = {"objectnode": next_12_letters_cleaned1}
            #database.set(data)


        else:
            print('No "Object" found in the string.')
        
        
        # Print the cleaned string for reference
        ###print('Cleaned String:', cleaned_string)

            

        
 
        #print(detection_result_list[0])
        #print(detection_result_list)
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


