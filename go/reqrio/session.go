//go:build cgo

package reqrio

/*
#include <stdint.h>
#include <stdbool.h>
#include <stdlib.h>

#cgo LDFLAGS: -L${SRCDIR}/../ -lreqrio
//-----------------[ScReq API]-----------------
extern void * ScReq_new(bool ignore_hdr_sort);
extern char * ScReq_set_header_json(void *req, const char *headers);
extern char * ScReq_add_header(void *req, const char *name, char *value, bool reversed);
extern char * ScReq_remove_header(void *req, const char *name);
extern char * ScReq_set_alpn(void *req, const char *alpn);
extern char * ScReq_set_verify(void *req, bool verify);
extern char * ScReq_set_redirect(void *req, bool redirect);
extern char * ScReq_set_key_log(void *req, const char *key_log);
extern char * ScReq_set_fingerprint(void *req, void * fingerprint);
extern char * ScReq_set_proxy(void *req, const char *alpn);
extern char * ScReq_set_timeout(void *req, const char *timeout);
extern char * ScReq_set_cookie(void *req, const char *cookie);
extern char * ScReq_add_cookie(void *req, const char *name, const char *value);
//callback
extern void * ScReq_do_http(void *req, int method, void *url, void * body, bool stream, char **err);
extern char * ScReq_reconnect(void *req);
extern char * ScReq_connect(void *req, const char *url, const char *sni);
extern char * ScReq_close_stream(void *req);
extern void ScReq_drop(void *req);

//-----------------------[Body API]---------------------------
extern void * Body_new(const uint8_t *data, size_t len, const char *ty, char **err);
extern void * Body_none();
extern void * Body_new_files(void *file, const char *data, char **err);
extern void * HttpFile_new();
extern char * HttpFile_add_form(void *file, void *form);
extern void * FileForm_new(const char *path, const char *field_name, const char *filetype, char **err);
extern void HttpFile_drop(void *file);
extern void Body_drop(void *body);



extern void char_free(char *);
extern char * url_encode(const char url);
*/
import "C"
import (
	"encoding/json"
	"errors"
	"net/url"
	"strings"
	"unsafe"
)

type ALPN string

const (
	HTTP20 ALPN = "h2"
	HTTP11 ALPN = "http/1.1"
)

type Session struct {
	req unsafe.Pointer
}

type ConnParam struct {
	Method      Method
	Url         string
	Sni         string
	Params      map[string]string
	Data        map[string]string
	Json        map[string]any
	Bytes       []byte
	Files       []HttpFile
	ContentType string
	Stream      bool
}

// NewSession /*
func NewSession(ignoreHdrSort bool) Session {
	p := C.ScReq_new(C.bool(ignoreHdrSort))
	return Session{req: p}
}

func (session *Session) SetHeaders(headers map[string]string) error {
	hdr, jeer := json.Marshal(headers)
	if jeer != nil {
		return jeer
	}
	cHeader := C.CString(string(hdr))
	defer C.free(unsafe.Pointer(cHeader))
	err := C.ScReq_set_header_json(session.req, cHeader)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))

	}
	return nil
}

func (session *Session) AddHeader(name string, value string, reversed bool) error {
	cName, cValue := C.CString(name), C.CString(value)
	defer C.free(unsafe.Pointer(cName))
	defer C.free(unsafe.Pointer(cValue))
	err := C.ScReq_add_header(session.req, cName, cValue, C.bool(reversed))
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) SetAlpn(alpn ALPN) error {
	cALpn := C.CString(string(alpn))
	defer C.free(unsafe.Pointer(cALpn))
	err := C.ScReq_set_alpn(session.req, cALpn)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) SetVerify(verify bool) error {
	err := C.ScReq_set_verify(session.req, C.bool(verify))
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) SetRedirect(autoRedirect bool) error {
	err := C.ScReq_set_redirect(session.req, C.bool(autoRedirect))
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) SetKeyLog(keyLog string) error {
	cKeyLog := C.CString(keyLog)
	defer C.free(unsafe.Pointer(cKeyLog))
	err := C.ScReq_set_key_log(session.req, cKeyLog)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) SetFingerprint(fingerprint Fingerprint) error {
	err := C.ScReq_set_fingerprint(session.req, fingerprint.rawPtr)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) SetProxy(proxy string) error {
	cProxy := C.CString(proxy)
	defer C.free(unsafe.Pointer(cProxy))
	err := C.ScReq_set_proxy(session.req, cProxy)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) SetTimeout(timeout string) error {
	cTimeout := C.CString(timeout)
	defer C.free(unsafe.Pointer(cTimeout))
	err := C.ScReq_set_timeout(session.req, cTimeout)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) SetCookie(cookie string) error {
	cCookie := C.CString(cookie)
	defer C.free(unsafe.Pointer(cCookie))
	err := C.ScReq_set_cookie(session.req, cCookie)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (session *Session) AddCookie(name string, value string) error {
	cName, cValue := C.CString(name), C.CString(value)
	defer C.free(unsafe.Pointer(cName))
	defer C.free(unsafe.Pointer(cValue))
	err := C.ScReq_add_cookie(session.req, cName, cValue)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func newContentBody(dataPtr *C.uchar, dataLen C.size_t, contentType *C.char) (unsafe.Pointer, error) {
	defer C.free(unsafe.Pointer(contentType))
	var errPtr *C.char
	bodyPtr := C.Body_new(dataPtr, dataLen, contentType, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return nil, errors.New(C.GoString(errPtr))
	}
	return bodyPtr, nil
}

func buildBody(param ConnParam) (unsafe.Pointer, error) {
	var dataPtr *C.uchar
	var contentType *C.char
	var dataLen C.size_t
	if param.Files != nil {
		return buildFileBody(param.Files, param.Data)
	} else if param.Data != nil {
		var builder strings.Builder
		for key, value := range param.Data {
			if builder.Len() > 0 {
				builder.WriteByte('&')
			}
			builder.WriteString(url.QueryEscape(key))
			builder.WriteByte('=')
			builder.WriteString(url.QueryEscape(value))
		}
		dataLen = C.size_t(builder.Len())
		dataPtr = (*C.uchar)(unsafe.Pointer(unsafe.StringData(builder.String())))
		if param.ContentType != "" {
			contentType = C.CString(param.ContentType)
		} else {
			contentType = C.CString("application/x-www-form-urlencoded")
		}
		return newContentBody(dataPtr, dataLen, contentType)
	} else if param.Json != nil {
		bytes, err := json.Marshal(param.Json)
		if err != nil {
			return nil, err
		}
		dataLen = C.size_t(len(bytes))
		dataPtr = (*C.uint8_t)(unsafe.Pointer(&bytes[0]))
		if param.ContentType != "" {
			contentType = C.CString(param.ContentType)
		} else {
			contentType = C.CString("application/json")
		}
		return newContentBody(dataPtr, dataLen, contentType)
	} else if param.Bytes != nil {
		dataLen = C.size_t(len(param.Bytes))
		dataPtr = (*C.uint8_t)(unsafe.Pointer(&param.Bytes[0]))
		if param.ContentType != "" {
			contentType = C.CString(param.ContentType)
		} else {
			contentType = C.CString("application/json")
		}
		return newContentBody(dataPtr, dataLen, contentType)
	}
	bodyPtr := C.Body_none()
	return bodyPtr, nil

}

func (session *Session) SendRequest(param ConnParam) (Response, error) {
	gUrl, err := buildUrl(param.Url, param.Params, param.Sni)
	if err != nil {
		return Response{}, err
	}
	bodyPtr, err := buildBody(param)
	if err != nil {
		gUrl.delete()
		return Response{}, err
	}

	var errPtr *C.char
	ptr := C.ScReq_do_http(session.req, C.int(param.Method), gUrl.ptr, bodyPtr, C.bool(param.Stream), (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		errMsg := C.GoString(errPtr)
		C.char_free(errPtr)
		return Response{}, errors.New(errMsg)
	}
	return newResponse(ptr, session.req)
}

func (session *Session) Reconnect() error {
	err := C.ScReq_reconnect(session.req)
	if err != nil {
		errMsg := C.GoString(err)
		C.char_free(err)
		return errors.New(errMsg)
	}
	return nil
}

func (session *Session) Connect(url string, sni string) error {
	cUrl := C.CString(url)
	defer C.free(unsafe.Pointer(cUrl))
	cSni := C.CString(sni)
	defer C.free(unsafe.Pointer(cSni))
	err := C.ScReq_connect(session.req, cUrl, cSni)
	if err != nil {
		errMsg := C.GoString(err)
		C.char_free(err)
		return errors.New(errMsg)
	}
	return nil
}

func (session *Session) CloseStream() error {
	err := C.ScReq_close_stream(session.req)
	if err != nil {
		errMsg := C.GoString(err)
		C.char_free(err)
		return errors.New(errMsg)
	}
	return nil
}

func (session *Session) Close() {
	if session.req != nil {
		C.ScReq_drop(session.req)
		session.req = nil
	}
}
