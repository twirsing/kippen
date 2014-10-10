#!/usr/bin/python
try:
    import RPi.GPIO as GPIO
except RuntimeError:
    print("Error importing RPi.GPIO!  This is probably because you need superuser privileges.  You can achieve this by using 'sudo' to run your script")
import sys, getopt

def handle(switch, tostatus):
  GPIO.setwarnings(False)
  GPIO.setmode(GPIO.BCM)
  pinList = [2, 3, 4, 17]
  try:
    print 'switching gpio: ', pinList[switch], tostatus
    GPIO.setup(pinList[switch], GPIO.OUT) 
    if tostatus == "high":
      # print 'to: ', tostatus
      GPIO.output(pinList[switch], GPIO.LOW)
    elif tostatus == "low":
      # print 'to: ', tostatus
      GPIO.output(pinList[switch], GPIO.HIGH)
    elif tostatus == "toggle":
      GPIO.output(pinList[switch], not GPIO.input(pinList[switch]))
    # GPIO.cleanup()

  except KeyboardInterrupt:
    print "  Quit"
    # Reset GPIO settings
    GPIO.cleanup()

def main(argv):
   switch=0
   tostatus=''
   try:
      opts, args = getopt.getopt(argv,"hs:t:",["switch=","tostatus="])
   except getopt.GetoptError:
      print 'relaytest.py -s <switch> -t <tostatus>'
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print 'relaytest.py -s <switch> -t <tostatus>'
         sys.exit()
      elif opt in ("-s", "--switch"):
         switch = arg
      elif opt in ("-t", "--tostatus"):
         tostatus = arg
   
   handle(int(switch), tostatus)
   # handle(1, "high")
if __name__ == "__main__":
   main(sys.argv[1:])





