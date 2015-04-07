class JsonClient(JsonSocket):
    def __init__(self, address='127.0.0.1', port=5489):
        super(JsonClient, self).__init__(address, port)
 
    def connect(self):
        for i in range(10):
            try:
                self.socket.connect( (self.address, self.port) )
            except socket.error as msg:
                logger.error("SockThread Error: %s" % msg)
                time.sleep(3)
                continue
            logger.info("...Socket Connected")
            return True
        return False
