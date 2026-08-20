//go:build cgo

package reqrio

/*
#include <stdint.h>
#include <stdbool.h>
#include <stdlib.h>

#cgo LDFLAGS: -L${SRCDIR}/../ -lreqrio
//-----------------[Url API]-----------------
extern uint16_t Response_status_code(const void *resp, char **err);
extern const uint8_t * Response_bytes(const void *resp, size_t *len, char **err);
extern char * Response_get_header(const void *resp, const char *name, char **err);
extern char * Response_cookies(const void *resp, char **err);
extern uint64_t Response_sid(const void *resp, char **err);
extern void Response_drop(void *resp);
extern void * ScReq_recv_stream(void *req, uint64_t sid, size_t *len, char **err);
extern void char_free(char *);
*/
import "C"
import (
	"encoding/json"
	"errors"
	"unsafe"
)

type Response struct {
	ptr     unsafe.Pointer
	req_ptr unsafe.Pointer
	sid     C.uint64_t
	err     error
}

func newResponse(ptr unsafe.Pointer, req_ptr unsafe.Pointer) (Response, error) {
	var errPtr *C.char
	sid := C.Response_sid(ptr, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return Response{}, errors.New(C.GoString(errPtr))
	}
	return Response{ptr: ptr, req_ptr: req_ptr, sid: sid, err: nil}, nil
}

func (resp *Response) StatusCode() (int16, error) {
	var errPtr *C.char
	code := C.Response_status_code(resp.ptr, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return 0, errors.New(C.GoString(errPtr))
	}
	return int16(code), nil
}

func (resp *Response) Bytes() ([]byte, error) {
	var errPtr *C.char
	var length C.size_t
	bytePtr := C.Response_bytes(resp.ptr, &length, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return nil, errors.New(C.GoString(errPtr))
	}
	bytes := C.GoBytes(unsafe.Pointer(bytePtr), C.int(length))
	return bytes, nil
}

func (resp *Response) Text() (string, error) {
	bytes, err := resp.Bytes()
	if err != nil {
		return "", err
	}
	return string(bytes), nil
}

func (resp *Response) GetHeader(name string) (string, error) {
	cName := C.CString(name)
	defer C.free(unsafe.Pointer(cName))
	var errPtr *C.char
	valuePtr := C.Response_get_header(resp.ptr, cName, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return "", errors.New(C.GoString(errPtr))
	}
	defer C.char_free(valuePtr)
	return C.GoString(valuePtr), nil
}

func (resp *Response) Delete() {
	if resp.ptr != nil {
		C.Response_drop(resp.ptr)
		resp.ptr = nil
	}
}

func (resp *Response) Cookies() ([]Cookie, error) {
	var errPtr *C.char
	cookiePtr := C.Response_cookies(resp.ptr, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return nil, errors.New(C.GoString(errPtr))
	}
	defer C.char_free(cookiePtr)
	cookieStr := C.GoString(cookiePtr)
	var cookies []Cookie
	err := json.Unmarshal([]byte(cookieStr), &cookies)
	if err != nil {
		return nil, err
	}
	return cookies, nil
}

func (resp *Response) Chunks() func(func([]byte) bool) {
	return func(yield func([]byte) bool) {
		for {
			var errPtr *C.char
			var length C.size_t
			ptr := C.ScReq_recv_stream(resp.req_ptr, resp.sid, &length, (**C.char)(unsafe.Pointer(&errPtr)))
			if errPtr != nil {
				defer C.char_free(errPtr)
				resp.err = errors.New(C.GoString(errPtr))
			}
			if ptr == nil {
				return
			}
			bytes := C.GoBytes(unsafe.Pointer(ptr), C.int(length))
			if !yield(bytes) {
				return
			}
		}
	}
}

func (resp *Response) Err() error {
	return resp.err
}
