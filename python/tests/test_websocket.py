def test_websocket():
    headers = {
        'Origin': 'https://poe.game.qq.com',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0',
        'Cookie': "pac_uid=0_NattYaCs7NNmH; omgid=0_NattYaCs7NNmH; _qimei_uuid42=19c1f11150d1000f92fe16d850a9c40cf94ef1d39f; _qimei_fingerprint=f3dc39297e432b1f08da57e9904a8f52; _qimei_q36=; _qimei_h38=a549811f92fe16d850a9c40c02000006b19c1f; _qpsvr_localtk=0.2296543129537577; RK=WPZCq/wl3I; ptcz=c338dead622f05f0d8467ac10589e7e45326b81d67ff476b9643f933cfdc644a; eas_sid=M1b7q677w9D5R5P2L8x5g4p313; eas_entry=https%3A%2F%2Fgraph.qq.com%2F; POESESSID=939e23af876572a0b2852b2e183e20cc"
    }

    ws = WebSocket(
        'wss://alive.github.com',
        uri="wss://poe.game.qq.com/api/trade2/live/poe2/%E7%93%A6%E5%B0%94%E7%9A%84%E5%AE%BF%E5%91%BD/32Y6Wjkc5",
        headers=headers)
    ws.close()
    ws.open()
    while True:
        frame = ws.read()
        print(frame.opcode, WsOpCode.PING, frame.opcode == WsOpCode.PING)
        if frame.opcode == WsOpCode.PING:
            print("ping")
            ws.write(WsOpCode.PONG, frame.payload)
        else:
            print(frame.payload.decode('utf-8'))
