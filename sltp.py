import os

videoFile = "/home/pi/tf_pi/Translating/inputdata/socket.mp4"
predict = "python3 predict.py --input_data_path='home/pi/tf_pi/Translating/' --ouput_data_path='home/pi/tf_pi/Translated/'"

while True:
    if os.path.isfile(videoFile):
        break
    
os.system(predict)
os.remove(videoFile)