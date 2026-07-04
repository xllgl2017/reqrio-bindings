package reqrio

/*
#include <stdint.h>
#include <stdbool.h>

extern void * ws_build();
extern int ws_add_header(void *builder, const char *name, const char *value);
extern int ws_set_proxy(void *builder, const char *proxy);
extern int ws_set_url(void *builder, const char *url);
extern int ws_set_uri(void *builder, const char *uri);
extern void *ws_open(void *builder);
extern void *ws_open_raw(const char *url, const char *raw);
extern char *ws_read(void *ws);
extern int ws_write(void *ws, int opcode, bool mask, const char *msg);
extern void ws_close(void *ws);
*/
import "C"

//type WebSocket struct {
//	builder unsafe.Pointer
//	ws      unsafe.Pointer
//}
//
//func BuildWebSocket(url string) (WebSocket, error) {
//	builder := C.ws_build()
//	ws := WebSocket{builder, unsafe.Pointer(C.CString(url))}
//	err := ws.SetUrl(url)
//	if err != nil {
//		return ws, err
//	}
//	return ws, nil
//}
//
//func (ws *WebSocket) AddHeader(name string, value string) error {
//	ret := C.ws_add_header(ws.builder, C.CString(name), C.CString(value))
//	if ret == -1 {
//		return errors.New("WebSocket AddHeader failed")
//	}
//	return nil
//}
//
//func (ws *WebSocket) SetProxy(proxy string) error {
//	ret := C.ws_set_proxy(ws.builder, C.CString(proxy))
//	if ret == -1 {
//		return errors.New("WebSocket SetProxy failed")
//	}
//	return nil
//}
//
//func (ws *WebSocket) SetUrl(url string) error {
//	ret := C.ws_set_url(ws.builder, C.CString(url))
//	if ret == -1 {
//		return errors.New("WebSocket set url error")
//	}
//	return nil
//}
//
//func (ws *WebSocket) SetUri(uri string) error {
//	ret := C.ws_set_uri(ws.builder, C.CString(uri))
//	if ret == -1 {
//		return errors.New("WebSocket set uri error")
//	}
//	return nil
//}
//
//func (ws *WebSocket) Open() error {
//	ret := C.ws_open(ws.builder)
//	if ret == nil {
//		return errors.New("WebSocket open failed")
//	}
//	return nil
//}
//
//func OpenRaw(url string, raw string) (WebSocket, error) {
//	var ws WebSocket
//	ret := C.ws_open_raw(C.CString(url), C.CString(raw))
//	if ret == nil {
//		return ws, errors.New("WebSocket OpenRaw failed")
//	}
//	ws.ws = ret
//	return ws, nil
//}
//
//func (ws *WebSocket) Read() string {
//	ret := C.ws_read(ws.ws)
//	text := C.GoString(ret)
//	return text
//}
//
//func (ws *WebSocket) Write(opcode int, mask bool, text string) error {
//	ret := C.ws_write(ws.ws, C.int(opcode), C.bool(mask), C.CString(text))
//	if ret == -1 {
//		return errors.New("WebSocket Write failed")
//	}
//	return nil
//}
//
//func (ws *WebSocket) Close() {
//	C.ws_close(ws.ws)
//}
