import reqrio
from reqrio import *

headers = {
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
    "Accept-Encoding": "gzip, deflate, br, zstd",
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    "Cache-Control": "no-cache",
    "Connection": "keep-alive",
    "Cookie": "__guid=15015764.1071255116101212729.1764940193317.2156; env_webp=1; _S=pvc5q7leemba50e4kn4qis4b95; QiHooGUID=4C8051464B2D97668E3B21198B9CA207.1766289287750; count=1; so-like-red=2; webp=1; so_huid=114r0SZFiQcJKtA38GZgwZg%2Fdit1cjUGuRcsIL2jTn4%2FE%3D; __huid=114r0SZFiQcJKtA38GZgwZg%2Fdit1cjUGuRcsIL2jTn4%2FE%3D; gtHuid=1",
    "Host": "m.so.com",
    "Pragma": "no-cache",
    "Sec-Fetch-Dest": "document",
    "Sec-Fetch-Mode": "navigate",
    "Sec-Fetch-Site": "none",
    "Sec-Fetch-User": "?1",
    "Upgrade-Insecure-Requests": 1,
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0",
    "sec-ch-ua": '"Microsoft Edge";v="143", "Chromium";v="143", "Not A(Brand";v="24"',
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": '"Windows"'
}


# ============================>Session Method<================================


def get():
    print("===========>reqrio [GET]<============")
    session = Session(headers)
    session.add_header("s", "l")
    session.connect('https://www.baidu.com')
    resp = session.get("https://www.baidu.com")
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))
    #
    resp = reqrio.get("https://www.baidu.com", headers)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def get_with_params():
    print("===========>reqrio [GET Params]<============")
    session = Session(headers)
    params = {
        'a': 3,
        "b": {'fgfdg': 'dg'}
    }
    resp = session.get("https://www.baidu.com", params)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))
    session.reconnect()
    #
    resp = reqrio.get("https://www.baidu.com", headers, params=params)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def post_form():
    print("===========>reqrio [POST Form]<============")
    session = Session(headers)
    data = {
        'a': 3,
        "b": {'fgfdg': 'dg'}
    }
    resp = session.post("https://www.baidu.com", data=data)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))
    #
    resp = reqrio.post("https://www.baidu.com", headers, data=data)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def post_json():
    print("===========>reqrio [POST Json]<============")
    session = Session(headers)
    data = {
        'a': 3,
        "b": {'fgfdg': 'dg'}
    }
    resp = session.post("https://www.baidu.com", json=data)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))
    #
    resp = session.post("https://www.baidu.com", headers, json=data)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def post_text():
    session = Session(headers)
    resp = session.post("https://www.baidu.com", text="test req body text")
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def upload_file():
    session = Session(headers)
    files = [
        {
            "path": "../../README.md",  # 文件的路径，可以是相对路径和绝对路径
            "field_name": "file",  # 文件的属性名，如浏览器预览: file: [二进制]
            "filetype": "text/plain"  # 设置文件类型
        }
    ]
    data = {
        "age": 10,
        "name": "test"
    }
    resp = session.post("https://www.baidu.com", data=data, files=files)
    print('code: ', resp.statue_code())
    print('body: ', len(resp.bytes()))


def flow_reader():
    session = Session(headers)
    resp = session.get("https://www.baidu.com", stream=True)
    print(resp.statue_code())
    for chunk in resp.chunks():
        print(chunk)
    resp = reqrio.get("https://ms.bdimg.com/pacific/0/pic/-742236409_-1564646186.png?x=0&y=0&h=340&w=510&vh=340.00&vw=510.00&oh=340.00&ow=510.00", stream=True)
    print(resp.statue_code())
    for chunk in resp.chunks():
        print(chunk)
