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
extern void Response_drop(void *resp);
extern void char_free(char *);
*/
import "C"
import (
	"encoding/json"
	"errors"
	"unsafe"
)

type Response struct {
	ptr unsafe.Pointer
}

func newResponse(ptr unsafe.Pointer) Response {
	return Response{ptr: ptr}
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
