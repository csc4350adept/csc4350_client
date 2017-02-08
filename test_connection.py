import socket
import ssl
import time

host = 'localhost'
port = 465

socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket.settimeout(5)
ssl_socket = ssl.wrap_socket(socket, ssl_version=ssl.PROTOCOL_TLSv1)
ssl_socket.connect((host, port))

msg = 'hello world\nmulti-line input\ngoodbye world\n'
msg2 = 'this is a second message\nit has super secret stuff in it'

ssl_socket.send(msg)
time.sleep(5)
ssl_socket.send(msg2)

ssl_socket.close()