from queue import Queue
from threading import Thread

from _queue import Empty

from reqrio._session import Session
from reqrio.method import Method


class Stream:
    def __init__(self, session: Session, method: Method, url: str, params: dict = None, data: dict = None,
                 json: dict = None, bs: bytes = None, content_type: str = None):
        self.session = session
        self.q = Queue()
        self._cb = session.callback(self._callback)
        self.thread = Thread(target=self.__start_stream)
        self.method = method
        self.response = None
        self.url = url
        self.params = params
        self.data = data
        self.json = json
        self.bs = bs
        self.ct = content_type
        self.start()

    def __start_stream(self):
        if self.method == Method.GET:
            self.response = self.session.get(self.url, self.params, self.data, self.json, self.bs, self.ct)
        elif self.method == Method.POST:
            self.response = self.session.get(self.url, self.params, self.data, self.json, self.bs, self.ct)
        elif self.method == Method.PUT:
            self.response = self.session.put(self.url, self.params, self.data, self.json, self.bs, self.ct)
        elif self.method == Method.HEAD:
            self.response = self.session.head(self.url, self.params, self.data, self.json, self.bs, self.ct)
        elif self.method == Method.OPTIONS:
            self.response = self.session.options(self.url, self.params, self.data, self.json, self.bs, self.ct),
        elif self.method == Method.TRACE:
            self.response = self.session.trace(self.url, self.params, self.data, self.json, self.bs, self.ct)
        elif self.method == Method.PATCH:
            self.response = self.session.patch(self.url, self.params, self.data, self.json, self.bs, self.ct)

    # 这个是 ctypes 回调
    def _callback(self, p, l):
        data = bytes(p[:l])
        self.q.put(data)
        return 0

    # 开始接收数据
    def start(self):
        r = self.session.dll.ScReq_set_callback(self.session.hid, self._cb)
        if r != 0:
            raise RuntimeError("register failed")
        self.thread.start()
        return self

    def __iter__(self):
        return self

    def __next__(self):
        if not self.thread.is_alive():
            raise StopIteration
        try:
            item = self.q.get(timeout=0.1)
        except Empty:
            return self.__next__()
        return item
