import SocketServer
import subprocess
import json

class MyTCPHandler(SocketServer.BaseRequestHandler):
    """
    The RequestHandler class for our server.

    It is instantiated once per connection to the server, and must
    override the handle() method to implement communication to the
    client.
    """

    def handle(self):
        # self.request is the TCP socket connected to the client
        data = self.request.recv(1024).strip()
        # print "{} wrote:".format(self.client_address[0])
        # print data
        j = json.loads(data)
        print j
        if j['command'] == 'foto':
            print "making foto"
            subprocess.call(['./_capture.sh'])
        elif j['command'] == 'switchrelay':
            print "switching relay"
            subprocess.call(['python', './switchrelay.py', '-s', j['channel'], '-t', j['switchcommand']])
        
        
        # just send back the same data, but upper-cased        
        self.request.sendall("ok")

if __name__ == "__main__":
    HOST, PORT = '', 9999

    # Create the server, binding to localhost on port 9999
    server = SocketServer.TCPServer((HOST, PORT), MyTCPHandler)

    # Activate the server; this will keep running until you
    # interrupt the program with Ctrl-C
    server.serve_forever()
