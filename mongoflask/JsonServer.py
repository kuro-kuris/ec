class JsonServer(JsonSocket):
    def __init__(self, address='127.0.0.1', port=5489):
        super(JsonServer, self).__init__(address, port)
        self._bind()
 
    def _bind(self):
        self.socket.bind( (self.address,self.port) )
 
    def _listen(self):
        self.socket.listen(1)
 
    def _accept(self):
        return self.socket.accept()
 
    def acceptConnection(self):
        self._listen()
 
        self.conn, addr = self._accept()
        self.conn.settimeout(self.timeout)
        logger.debug("connection accepted, conn socket (%s,%d)" % (addr[0],addr[1]))
