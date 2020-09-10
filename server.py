import socket
import struct
import sys
import time

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(('',5172)) #5172 => sltp
server.listen(5)

while True:
    client, address = server.accept()
    print("connected", address);
    data = ""
                        
    fileSize = client.recv(8) 
    #print(fileSize) #: b'\x00\x00\x......'
    #size = int.from_bytes(fileSize, "big") #python 3.x
    size = struct.unpack(">q", fileSize)[0] #python 2.x   
        
    img_file = open("socket.mp4", "wb")
       
    while True:
        img_data = client.recv(1024)
        data += img_data #python 2.x
        
        if (sys.getsizeof(data)>=size):
            break
        if not img_data:
            break
        #print("receiving Images")
        
    print(size)
    img_file.write(data)
    img_file.close()
        
    text_file = open("result.txt","r")
    string = text_file.read()
    text_file.close()
        
    client.send(string.decode('utf-8').encode('euc-kr'))
    client.close()
