import snowboydecoder
import sys
import signal
from light import Light

interrupted = False

def signal_handler(signal, frame):
    global interrupted
    interrupted = True

def interrupt_callback():
    global interrupted
    return interrupted


signal.signal(signal.SIGINT, signal_handler)

detector = snowboydecoder.HotwordDetector("ok.pmdl", sensitivity=0.44, audio_gain=11)
print('Listening... Press Ctrl+C to exit')

led = Light(17)
detector.start(detected_callback=led.blink,
               interrupt_check=interrupt_callback,
               sleep_time=0.03)

detector.terminate()
