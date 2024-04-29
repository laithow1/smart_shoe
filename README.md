# smart-shoe
 Introduction
Object detection on edge computers has become a pivotal field in computer vision, enabling devices to autonomously perceive and respond to their surroundings. EfficientDet, a state-of-the-art object detection model, takes center stage in this domain. Designed to be resource-efficient, it strikes a balance between accuracy and computational demands, making it particularly well-suited for deployment on edge devices with limited processing power. Object detection on edge computers, exemplified by EfficientDet, finds applications in scenarios ranging from smart surveillance cameras and autonomous vehicles to Internet of Things (IoT) devices. Its ability to identify and locate multiple objects in real-time makes it a key enabler for enhancing the autonomy and intelligence of edge computing systems.

Getting Start
Before you start this project, you may need to prepare your hardware and software in advance as described here.

Hardware preparation
reTerminal	PiCam


Get One Now 🖱️
📚 Learn More
Software Preparation
We recommend installing the Bullesye or Bookworm version of Raspberry Pi 64 bit OS from their official website. If you prefer to install a new Raspbian OS, please follow the steps outlined in this guide.

NOTE
We highly recommend checking out our previous tutorial on Getting started with OpenCV, as this tutorial serves as a continuation in our series. This guide is specifically for Bulleseye OS and Bookworm OS. Please take note of the installation dependencies, as there are two distinct methods outlined here.

TinyML
TinyML revolutionizes machine learning by enabling lightweight models to run on edge devices with minimal resources. In the context of object detection, TensorFlow Lite, a key framework in TinyML, optimizes models for efficiency, allowing them to be deployed on devices like microcontrollers and embedded systems. This integration facilitates real-time, on-device processing for tasks such as object recognition, making TinyML with TensorFlow Lite ideal for applications in smart sensors, wearables, and IoT devices without the need for constant cloud connectivity.

EfficientDet
EfficientDet is a highly efficient and accurate object detection model that stands out for its performance on edge devices. Developed by Google, EfficientDet optimizes the balance between model accuracy and computational efficiency, making it well-suited for deployment on resource-constrained environments such as edge devices and mobile platforms. It leverages a compound scaling method to efficiently scale up model parameters and achieve better accuracy without compromising speed. EfficientDet's architecture includes a feature network for capturing image features effectively and a compound scaling method for balancing model accuracy and computational efficiency. Its success lies in achieving impressive performance on various object detection benchmarks while maintaining a lightweight structure, making it a go-to choice for applications requiring real-time object detection on edge devices.

Let's run the code in Bullseye OS.
Make sure that you are in correct folder. If not

cd laith_Python_ReTerminal/samples/Opencv_and_piCam/ObjectDetection

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
git clone [https://github.com/laithow1/laith_Python_ReTerminal](https://github.com/laithow1/laith_Python_ReTerminal-main)
```
Step 3 Next, utilize our script to effortlessly install the required Python packages and download the EfficientDet-Lite model. Navigate to this folder.
```
cd laith_Python_ReTerminal/samples/Opencv_and_piCam/ObjectDetection_bookworm
```
Step 3 The script install the required dependencies and download the TFLite models For this tutorial series.
```
sh setup.sh
```
Run the code
```
python3 detect_picam.py
```
# smart-shoe
# Python Library for reTerminal

## Installation

### From PyPI

- To install the latest release from PyPI
```
sudo pip3 install laith-python-reterminal
```

### From Source

- To install from source, clone this repository
```
git clone https://github.com/laithow1/laith_Python_ReTerminal
```

- Install the library 

```
cd laith_Python_ReTerminal
sudo pip3 install .
```

## Usage

### User LEDs Test

```python
import laith_python_reterminal.core as rt
import time

print("STA ON, USR OFF")
rt.sta_led = True
rt.usr_led = False
time.sleep(1)

print("STA OFF, USR ON")
rt.sta_led = False
rt.usr_led = True
time.sleep(1)

print("STA RED, USR OFF")
rt.sta_led_green = False
rt.sta_led_red = True
rt.usr_led = False
time.sleep(1)

print("STA OFF, USR OFF")
rt.sta_led = False
rt.usr_led = False
```

### Buzzer Test

```python
import laith_python_reterminal.core as rt
import time

print("BUZZER ON")
rt.buzzer = True
time.sleep(1)

print("BUZZER OFF")
rt.buzzer = False
```

### User Buttons Test

```python
import laith_python_reterminal.core as rt
import laith_python_reterminal.button as rt_btn


device = rt.get_button_device()
while True:
    for event in device.read_loop():
        buttonEvent = rt_btn.ButtonEvent(event)
        if buttonEvent.name != None:
            print(f"name={str(buttonEvent.name)} value={buttonEvent.value}")
```

### Accelerometer Test

```python
import laith_python_reterminal.core as rt
import laith_python_reterminal.acceleration as rt_accel


device = rt.get_acceleration_device()
while True:
    for event in device.read_loop():
        accelEvent = rt_accel.AccelerationEvent(event)
        if accelEvent.name != None:
            print(f"name={str(accelEvent.name)} value={accelEvent.value}")
```

### Accelerometer and Buttons Test

```python
import asyncio
import laith_python_reterminal.core as rt
import laith_python_reterminal.acceleration as rt_accel
import laith_python_reterminal.button as rt_btn


async def accel_coroutine(device):
    async for event in device.async_read_loop():
        accelEvent = rt_accel.AccelerationEvent(event)
        if accelEvent.name != None:
            print(f"accel name={str(accelEvent.name)} value={accelEvent.value}")


async def btn_coroutine(device):
    async for event in device.async_read_loop():
        buttonEvent = rt_btn.ButtonEvent(event)
        if buttonEvent.name != None:
            print(f"name={str(buttonEvent.name)} value={buttonEvent.value}")


accel_device = rt.get_acceleration_device()
btn_device = rt.get_button_device()

asyncio.ensure_future(accel_coroutine(accel_device))
asyncio.ensure_future(btn_coroutine(btn_device))

loop = asyncio.get_event_loop()
loop.run_forever()
```

### Illuminance Sensor Test
```python
import time
import laith_python_reterminal.core as rt

while True:
    print(rt.illuminance)
    time.sleep(0.2)
```

**The Following Test Should Work With Reterminal Bridge**

### fan Test

```python
import laith_python_reterminal.core as rt
import time

print("FAN ON")
rt.fan = True
time.sleep(1)

print("FAN OFF")
rt.fan = False
```

### RS232 Test

```python
import sys
import serial
import time
import laith_python_reterminal.core as rt

param1 = sys.argv[1]

# enable the rs232 for test
rt.rs232_or_rs485 = "RS232"

# init the serial
ser = serial.Serial(
    port='/dev/ttyS0',
    baudrate = 9600,
    parity=serial.PARITY_NONE,
    stopbits=serial.STOPBITS_ONE,
    bytesize=serial.EIGHTBITS,
    timeout=1
)

if param1 == "send":
    counter=0
    try:
        print("rs232 starts now!\n")
        ser.write("rs232 starts now!\n".encode())
        while 1:
                ser.write(("Write counter:{}\n".format(counter)).encode())
                time.sleep(1)
                counter += 1
    except KeyboardInterrupt:
        exit()
elif param1 == "receive":
    try:
        print("Start receiving data now!\n")
        while 1:
            x=ser.readline()
            if x != b'':
                print(x)
    except KeyboardInterrupt:
        exit()
else:
    print('param input error,try again with send or receive')
```
**Note:**:When we use the test script of RS232/RS485/CAN.We need to pass a parameter to them.

Take the RS232 for example:
```
python3 test_rs232.py send # test the send(TX) function of RS232
python3 test_rs232.py receive # test the receive(RX) function of RS232
```

### RS485 Test

```python
import sys
import serial
import time
import laith_python_reterminal.core as rt

param1 = sys.argv[1]

# enable the rs485 for test
rt.rs232_or_rs485 = "RS485"

# init the serial
ser = serial.Serial(
    port='/dev/ttyS0',
    baudrate = 9600,
    parity=serial.PARITY_NONE,
    stopbits=serial.STOPBITS_ONE,
    bytesize=serial.EIGHTBITS,
    timeout=1
)

if param1 == "send":
    counter=0
    # enable the rs485 for tx
    rt.rs485_tx_rx_stat = "TX"
    try:
        print("rs485 starts now!\n")
        ser.write("rs485 starts now!\n".encode())
        while 1:
                ser.write(("Write counter:{}\n".format(counter)).encode())
                time.sleep(1)
                counter += 1
    except KeyboardInterrupt:
        exit()
elif param1 == "receive":
    # enable the rs485 for rx
    rt.rs485_tx_rx_stat = "RX"
    try:
        print("Start receiving data now!\n")
        while 1:
            x=ser.readline()
            if x != b'':
                print(x)
    except KeyboardInterrupt:
        exit()
else:
    print('param input error,try again with send or receive')
```

### CAN Test

```python
# NOTICE: please make sure you have pip3 install python-can
#         before you use this test script
# import the library
import can
import sys
import time

param1 = sys.argv[1]

# create a bus instance
# many other interfaces are supported as well (see documentation)
bus = can.Bus(interface='socketcan',
              channel='can0',
              receive_own_messages=True)

if param1 == "send":
    # send a message
    counter=0
    print("can send starts now!\n")
    try:
        while True:
            message = can.Message(arbitration_id=123, is_extended_id=True,
                      data=[0x11, 0x22, counter])
            bus.send(message, timeout=0.2)
            time.sleep(1)
            counter += 1
    except KeyboardInterrupt:
        exit()

elif param1 == "receive":
    # iterate over received messages
    try:
        for msg in bus:
            print(f"{msg.arbitration_id:X}: {msg.data}")
    except KeyboardInterrupt:
        exit()
else:
    print('param input error,try again with send or receive')
```
**Note:** Please make sure your CAN interface is working before run this script.
If not. You will get the error log with "Network is down". And you can 
enable the can with "sudo ip link set can0 up type can bitrate 500000".

## API Reference

- **usr_led**: Turn on/off green USR LED

```python
rt.usr_led = True #Turn on green USR LED
rt.usr_led = False #Turn off green USR LED
```

- **sta_led_red**: Turn on/off red STA LED

```python
rt.sta_led_red = True #Turn on red STA LED
rt.sta_led_red = False #Turn off red STA LED
```

- **sta_led_green**: Turn on/off green STA LED

```python
rt.sta_led_green = True #Turn on green STA LED
rt.sta_led_green = False #Turn off green STA LED
```

**Note:** If red STA LED is on during this time, the green STA LED will turn on over the red STA LED

- **sta_led**: Turn on/off green STA LED

```python
rt.sta_led = True #Turn on green STA LED
rt.sta_led = False #Turn off green STA LED
```

**Note:** If red STA LED is on during this time, the green STA LED will turn on and the red STA LED will turn off

- **buzzer** : Turn on/off buzzer

```python
rt.buzzer = True #Turn on buzzer
rt.buzzer = False #Turn off buzzer
```

- **get_button_device()**: Obtain information about the buttons including all the events supported by them

```python
device = rt.get_button_device()
```

- **ButtonEvent()**: Calls the ButtonEvent() and returns the EVENT

```python
buttonEvent = rt_btn.ButtonEvent(event)
```

- **get_acceleration_device()**: Obtain information about the accelerometer including all the events supported by it 

```python
device = rt.get_acceleration_device()
```

- **AccelerationEvent()**: Calls the AccelerationEvent() and returns the EVENT

```python
accelEvent = rt_accel.AccelerationEvent(event)
```

- **Illuminance** :Obtain the current value from the illuminance sensor
```python
illuminance = rt.illuminance
```

- **fan**: Turn on/off fan

```python
rt.fan = True # Turn on fan
rt.fan = False # Turn off fan
```

- **rs232_or_rs485**: Open the RS232 or RS485

```python
rt.rs232_or_rs485 = "RS232" # open the RS232
rt.rs232_or_rs485 = "RS485" # open the RS485
```

- **rs485_tx_rx_stat**: Switch the function between send(TX) and receive(Rx) of RS485

```python
rt.rs485_tx_rx_stat = "TX" # enable the send(TX) of RS485
rt.rs485_tx_rx_stat = "RX" # enable the receive(RX) of RS485
```

# smart-shoe
# smart-shoe
