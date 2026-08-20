import ctypes
from ctypes import c_char_p, c_size_t, byref
from queue import Queue
from threading import Thread
from reqrio.bindings import DLL
from reqrio import util
from _queue import Empty


class StreamChunk:
    def __init__(self, sid, req):
        self.q = Queue()
        self.thread = Thread(target=self.__start_stream)
        self.sid = sid
        self.req = req
        self.thread.start()

    def __start_stream(self):
        while True:
            err = c_char_p()
            len = c_size_t()
            ptr = DLL.ScReq_recv_stream(self.req, self.sid, byref(len), byref(err))
            err, msg = util.check_char_err(err)
            if err: raise Exception(msg)
            if ptr is None: break
            res = ctypes.string_at(ptr, len.value)
            self.q.put(res)

    def __iter__(self):
        return self

    def __next__(self):
        try:
            item = self.q.get(timeout=0.1)
        except Empty:
            if not self.thread.is_alive():
                raise StopIteration
            return self.__next__()
        return item
