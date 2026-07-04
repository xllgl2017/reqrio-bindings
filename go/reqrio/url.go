//go:build cgo

package reqrio

/*
#include <stdint.h>
#include <stdbool.h>
#include <stdlib.h>

#cgo LDFLAGS: -L${SRCDIR}/../ -lreqrio
//-----------------[Url API]-----------------
extern void * Url_new(const char *base_url, char **err);
extern char * Url_add_param(void *url, const char *name, const char *value);
extern char * Url_remove_param(void *url, const char *name);
extern char * Url_set_sni(void *url, const char *sni);
extern void Url_drop(void *req);
extern void char_free(char *);
*/
import "C"
import (
	"errors"
	"unsafe"
)

type Url struct {
	ptr unsafe.Pointer
}

func newUrl(url string) (Url, error) {
	var res Url
	var errPtr *C.char
	curl := C.CString(url)
	defer C.free(unsafe.Pointer(curl))
	res.ptr = C.Url_new(curl, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return res, errors.New(C.GoString(errPtr))
	}
	return res, nil
}

func (url *Url) addParam(name string, value string) error {
	cname := C.CString(name)
	cValue := C.CString(value)
	defer C.free(unsafe.Pointer(cname))
	defer C.free(unsafe.Pointer(cValue))
	err := C.Url_add_param(url.ptr, cname, cValue)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (url *Url) delParam(name string) error {
	cName := C.CString(name)
	defer C.free(unsafe.Pointer(cName))
	err := C.Url_remove_param(url.ptr, cName)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (url *Url) setSni(sni string) error {
	cSni := C.CString(sni)
	defer C.free(unsafe.Pointer(cSni))
	err := C.Url_set_sni(url.ptr, cSni)
	if err != nil {
		defer C.char_free(err)
		return errors.New(C.GoString(err))
	}
	return nil
}

func (url *Url) delete() {
	if url.ptr != nil {
		C.Url_drop(url.ptr)
		url.ptr = nil
	}
}

func buildUrl(baseUrl string, params map[string]string, sni string) (Url, error) {
	url, err := newUrl(baseUrl)
	if err != nil {
		return Url{}, err
	}
	if params != nil {
		for key, value := range params {
			err = url.addParam(key, value)
			if err != nil {
				url.delete()
				return Url{}, err
			}
		}
	}
	if sni != "" {
		err = url.setSni(sni)
		if err != nil {
			url.delete()
			return Url{}, err
		}
	}
	return url, nil
}
