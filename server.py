import socket

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(('', 9999))
server.listen(5)

while True:
	client, address = server.accept()
	print("connected", address);
	data = ""
	
	print("is it true or false")
	
	img_file = open("socket.mp4", "wb")
		
	while True:
            img_data = client.recv(1024)
        
            if not img_data:
                print("break here")
                break
        
            data += img_data
            print("receiving Images")
            
	img_file.write(data)
        img_file.close()
        client.close()
		
