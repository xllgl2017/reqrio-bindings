//go:build cgo

package reqrio

/*
#include <stdint.h>
#include <stdbool.h>
#include <stdlib.h>

#cgo LDFLAGS: -L${SRCDIR}/../ -lreqrio
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
	"unsafe"
)

type HttpFile struct {
	Path      string
	FieldName string
	Filetype  string
}

func buildFileBody(files []HttpFile, data map[string]string) (unsafe.Pointer, error) {
	if len(files) == 0 {
		return nil, errors.New("无文件")
	}
	filePtr := C.HttpFile_new()
	for _, file := range files {

		cPath := C.CString(file.Path)

		var cFieldName *C.char
		if file.FieldName != "" {
			cFieldName = C.CString(file.FieldName)
		} else {
			cFieldName = C.CString("file")
		}
		var cFiletype *C.char = nil
		if file.Filetype != "" {
			cFiletype = C.CString(file.Filetype)
		}
		var errPtr *C.char

		form := C.FileForm_new(cPath, cFieldName, cFiletype, (**C.char)(unsafe.Pointer(&errPtr)))
		C.free(unsafe.Pointer(cPath))
		if cFiletype != nil {
			C.free(unsafe.Pointer(cFiletype))
		}
		if errPtr != nil {
			errMsg := C.GoString(errPtr)
			C.char_free(errPtr)
			C.HttpFile_drop(filePtr)
			return nil, errors.New(errMsg)
		}

		err := C.HttpFile_add_form(filePtr, form)
		if err != nil {
			C.HttpFile_drop(filePtr)
			errMsg := C.GoString(err)
			C.char_free(err)
			return nil, errors.New(errMsg)
		}
	}
	var errPtr *C.char
	var dataPtr *C.char = nil
	if data != nil {
		dataByte, err := json.Marshal(data)
		if err != nil {
			C.HttpFile_drop(filePtr)
			return nil, err
		}
		dataPtr = C.CString(string(dataByte))
		defer C.free(unsafe.Pointer(dataPtr))
	}
	bodyPtr := C.Body_new_files(filePtr, dataPtr, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return nil, errors.New(C.GoString(errPtr))
	}
	return bodyPtr, nil
}
