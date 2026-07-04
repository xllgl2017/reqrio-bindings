//go:build cgo

package reqrio

/*
#include <stdint.h>
#include <stdbool.h>
#include <stdlib.h>

#cgo LDFLAGS: -L${SRCDIR}/../ -lreqrio
//-----------------[Fingerprint API]-----------------
extern void *Fingerprint_new(const char *token);
extern void Fingerprint_add_cipher_suite(void *finger, uint16_t suite);
extern void Fingerprint_add_ext(void *finger, uint16_t ext_typ);
extern void Fingerprint_add_ext_alpn(void *finger, uint16_t ext_typ, const char *alpn);
extern void Fingerprint_add_ext_version(void *finger, uint16_t ext_typ, uint16_t version);
extern void Fingerprint_add_ext_curve(void *finger, uint16_t ext_typ, uint16_t curve);
extern void Fingerprint_add_ext_compress(void *finger, uint16_t ext_typ, uint16_t method);
extern void Fingerprint_add_ext_psk_mode(void *finger, uint16_t ext_typ, uint8_t mode);
extern void Fingerprint_add_ext_padding(void *finger, uint16_t ext_typ, size_t padding);
extern void Fingerprint_add_ext_bytes(void *finger, uint16_t ext_typ, uint8_t *bytes, size_t len);
extern void Fingerprint_add_ext_algorithm(void *finger, uint16_t ext_typ, uint16_t algorithm);
extern void Fingerprint_add_ext_ec_point(void *finger, uint16_t ext_typ, uint8_t ec_point);
extern void Fingerprint_add_h2_setting(void *finger, uint16_t flag, uint32_t value);
extern void Fingerprint_set_h2_window_size(void *finger, uint32_t size);
extern void Fingerprint_set_h2_priority(void *finger, bool priority, uint8_t weight);
extern void *Fingerprint_from_ja3(const char *ja3, const char *token, char **err);
extern void *Fingerprint_from_ja4(const char *ja4, const char *token, char **err);
extern void *Fingerprint_from_client_hello(uint8_t *client_hello, size_t len, const char *token, char **err);
extern void *Fingerprint_random(const char *token, char **err);
extern void Fingerprint_drop(void *finger);
extern void char_free(char *);
*/
import "C"
import (
	"errors"
	"unsafe"
)

type Fingerprint struct {
	rawPtr unsafe.Pointer
}

func NewFingerprint(token string) Fingerprint {
	var fingerprint Fingerprint
	cToken := C.CString(token)
	defer C.free(unsafe.Pointer(cToken))
	fingerprint.rawPtr = C.Fingerprint_new(cToken)
	return fingerprint
}

func (fingerprint *Fingerprint) AddCipherSuites(suites []CipherSuite) {
	for _, suite := range suites {
		C.Fingerprint_add_cipher_suite(fingerprint.rawPtr, C.uint16_t(suite))
	}
}

func (fingerprint *Fingerprint) AddExtensionDefault(typ ExtensionType) {
	C.Fingerprint_add_ext(fingerprint.rawPtr, C.uint16_t(typ))
}

func (fingerprint *Fingerprint) AddExtAlps(typ ExtensionType, alps []ALPN) {
	for _, alp := range alps {
		cAlpn := C.CString(string(alp))
		C.Fingerprint_add_ext_alpn(fingerprint.rawPtr, C.uint16_t(typ), cAlpn)
		C.free(unsafe.Pointer(cAlpn))
	}
}

func (fingerprint *Fingerprint) AddExtVersions(typ ExtensionType, versions []Version) {
	for _, version := range versions {
		C.Fingerprint_add_ext_version(fingerprint.rawPtr, C.uint16_t(typ), C.uint16_t(version))
	}
}

func (fingerprint *Fingerprint) AddExtGroups(typ ExtensionType, groups []SupportGroup) {
	for _, group := range groups {
		C.Fingerprint_add_ext_curve(fingerprint.rawPtr, C.uint16_t(typ), C.uint16_t(group))
	}
}

func (fingerprint *Fingerprint) AddExtCompresses(typ ExtensionType, methods []CompressionMethod) {
	for _, method := range methods {
		C.Fingerprint_add_ext_compress(fingerprint.rawPtr, C.uint16_t(typ), C.uint16_t(method))
	}
}

func (fingerprint *Fingerprint) AddExtPskMode(typ ExtensionType, mode PskMode) {
	C.Fingerprint_add_ext_psk_mode(fingerprint.rawPtr, C.uint16_t(typ), C.uint8_t(mode))
}

func (fingerprint *Fingerprint) AddExtPadding(typ ExtensionType, padding int) {
	C.Fingerprint_add_ext_padding(fingerprint.rawPtr, C.uint16_t(typ), C.size_t(padding))
}

func (fingerprint *Fingerprint) AddExtBytes(typ ExtensionType, bytes []byte) {
	dataPtr := (*C.uint8_t)(unsafe.Pointer(&bytes[0]))
	C.Fingerprint_add_ext_bytes(fingerprint.rawPtr, C.uint16_t(typ), dataPtr, C.size_t(len(bytes)))
}

func (fingerprint *Fingerprint) AddExtAlgorithm(typ ExtensionType, algorithms []Algorithm) {
	for _, algo := range algorithms {
		C.Fingerprint_add_ext_algorithm(fingerprint.rawPtr, C.uint16_t(typ), C.uint16_t(algo))
	}
}

func (fingerprint *Fingerprint) AddExtEcPoint(typ ExtensionType, points []EcPointFormat) {
	for _, point := range points {
		C.Fingerprint_add_ext_ec_point(fingerprint.rawPtr, C.uint16_t(typ), C.uint8_t(point))
	}
}

func (fingerprint *Fingerprint) AddH2Setting(setting H2Setting, value uint32) {
	C.Fingerprint_add_h2_setting(fingerprint.rawPtr, C.uint16_t(setting), C.uint32_t(value))
}

func (fingerprint *Fingerprint) SetH2WindowSize(size uint32) {
	C.Fingerprint_set_h2_window_size(fingerprint.rawPtr, C.uint32_t(size))
}

func (fingerprint *Fingerprint) SetH2Priority(priority bool, weight uint8) {
	C.Fingerprint_set_h2_priority(fingerprint.rawPtr, C.bool(priority), C.uint8_t(weight))
}

func FromJa3(ja3 string, token string) (Fingerprint, error) {
	var fingerprint Fingerprint
	cJa3 := C.CString(ja3)
	defer C.free(unsafe.Pointer(cJa3))
	cToken := C.CString(token)
	defer C.free(unsafe.Pointer(cToken))
	var errPtr *C.char
	fingerprint.rawPtr = C.Fingerprint_from_ja3(cJa3, cToken, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return fingerprint, errors.New(C.GoString(errPtr))
	}
	return fingerprint, nil
}

func FromJa4(ja4 string, token string) (Fingerprint, error) {
	var fingerprint Fingerprint
	cJa4 := C.CString(ja4)
	defer C.free(unsafe.Pointer(cJa4))
	cToken := C.CString(token)
	defer C.free(unsafe.Pointer(cToken))
	var errPtr *C.char
	fingerprint.rawPtr = C.Fingerprint_from_ja4(cJa4, cToken, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return fingerprint, errors.New(C.GoString(errPtr))
	}
	return fingerprint, nil
}

func FromClientHello(clientHello []byte, token string) (Fingerprint, error) {
	var fingerprint Fingerprint
	dataPtr := (*C.uint8_t)(unsafe.Pointer(&clientHello[0]))
	cToken := C.CString(token)
	defer C.free(unsafe.Pointer(cToken))
	var errPtr *C.char
	fingerprint.rawPtr = C.Fingerprint_from_client_hello(dataPtr, C.size_t(len(clientHello)), cToken, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return fingerprint, errors.New(C.GoString(errPtr))
	}
	return fingerprint, nil
}

func RandomFingerprint(token string) (Fingerprint, error) {
	var fingerprint Fingerprint
	cToken := C.CString(token)
	defer C.free(unsafe.Pointer(cToken))
	var errPtr *C.char
	fingerprint.rawPtr = C.Fingerprint_random(cToken, (**C.char)(unsafe.Pointer(&errPtr)))
	if errPtr != nil {
		defer C.char_free(errPtr)
		return fingerprint, errors.New(C.GoString(errPtr))
	}
	return fingerprint, nil
}
