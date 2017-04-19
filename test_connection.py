import socket
import ssl
import time
import sys

host = 'localhost'
port = 993
#993 for imap

socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket.settimeout(5)
ssl_socket = ssl.wrap_socket(socket, ssl_version=ssl.PROTOCOL_TLSv1)
ssl_socket.connect((host, port))

while True:
    
    msg = raw_input()

    ssl_socket.write(msg)
    resp = ssl_socket.read()
    print resp
