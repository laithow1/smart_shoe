import RPi.GPIO as GPIO
import time
import pyrebase

# Firebase configuration (replace with your own credentials)
config = {
  "apiKey": "AIzaSyBcqdZCYT_QvN3ekr2rM52R_iVgCQMsSxE",
  "authDomain": "double-vehicle-419406.firebaseapp.com",
  "databaseURL": "https://double-vehicle-419406-default-rtdb.firebaseio.com",
  "projectId": "double-vehicle-419406",
  "storageBucket": "double-vehicle-419406.appspot.com",
  "messagingSenderId": "813850733586",
  "appId": "1:813850733586:web:073cde7f7bc67eb9053ccb",
  "measurementId": "G-2GZENZ1VSZ"
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
