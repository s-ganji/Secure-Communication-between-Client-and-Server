# Secure-Communication-between-Client-and-Server
Implementation of a secure tunnel between a client and server for massage sharing using Java language programming, Information Security final course project, Fall 2019 <br/>
## phase 1
- TCP connections have been stablished between each client and server, then files or messages have been shared between them with the use of symmetric algorithms.
- The AES cryptographic algorithm has been used for message or file sharing. A session key has been exchanged between each client and server with symmetric encription algorithm and then all messages have been sent with this key.
-  Each one of the servers and clients has a table to save a public key and session key for symmetric encription.
-  Data has been sent in Byte stream form.
## phase 2
- TCP connections have been stablished between each client and server, then files or messages have been shared between them with the use of asymmetric algorithms.
- The session key has been exchanged between each client and server with asymmetric encryption algorithm. 
- A two way PKC authentication and Digital signatures have been implemented.
- Each one of the servers and clients has a table to save their private and public key for asymmetric encription. For external servers, each client should save the server's name and public key. 
- All messages have been sent with the session key.
- Each key is expired after a given time, then a new key should be exchanged. 
- Data is sent in the Byte stream form.
