use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use std::ptr::null_mut;
use std::{mem, slice};
use reqrio::*;
use reqrio::base64::Base64;
use crate::HlsResult;

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn url_encode(url: *const c_char) -> *mut c_char {
    || -> HlsResult<*mut c_char> {
        let uri = unsafe { CStr::from_ptr(url) }.to_str()?;
        let res = coder::url_encode(uri);
        Ok(CString::new(res.into_owned())?.into_raw())
    }().unwrap_or(null_mut())
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn url_decode(url: *const c_char) -> *mut c_char {
    || -> HlsResult<*mut c_char> {
        let uri = unsafe { CStr::from_ptr(url) }.to_str()?;
        let res = coder::url_decode(uri)?;
        Ok(CString::new(res.into_owned())?.into_raw())
    }().unwrap_or(null_mut())
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn hex_encode(data: *const u8, data_len: usize) -> *mut c_char {
    || -> HlsResult<*mut c_char> {
        let data = unsafe { slice::from_raw_parts(data, data_len) };
        let res = hex::encode(data);
        Ok(CString::new(res)?.into_raw())
    }().unwrap_or(null_mut())
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn hex_decode(data: *const c_char, out: *mut *mut u8, len: &mut usize) -> i32 {
    || -> HlsResult<i32> {
        let uri = unsafe { CStr::from_ptr(data) }.to_str()?;
        let mut res = hex::decode(uri)?;
        unsafe {
            *out = res.as_mut_ptr();
            *len = res.len();
        }
        mem::forget(res);
        Ok(0)
    }().unwrap_or(-1)
}


#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub fn Base64_new() -> *mut Base64 {
    Box::into_raw(Box::new(Base64::default()))
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub fn Base64_encode(base64: *mut Base64, data: *const u8, len: usize) -> *mut c_char {
    || -> HlsResult<*mut c_char> {
        let base64 = unsafe { base64.as_mut() }.ok_or(RlsError::NullPtr)?;
        let data = unsafe { slice::from_raw_parts(data, len) };
        let res = base64.encode(data);
        Ok(CString::new(res)?.into_raw())
    }().unwrap_or(null_mut())
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub fn Base64_decode(base64: *mut Base64, data: *const u8, len: usize, out: *mut *mut u8, out_len: &mut usize) -> i32 {
    || -> HlsResult<i32> {
        let base64 = unsafe { base64.as_mut() }.ok_or(RlsError::NullPtr)?;
        let data = unsafe { slice::from_raw_parts(data, len) };
        let mut res = base64.decode(data)?;
        unsafe {
            *out = res.as_mut_ptr();
            *out_len = res.len();
        }
        mem::forget(res);
        Ok(0)
    }().unwrap_or(-1)
}


#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub fn Base64_free(base64: *mut Base64) {
    if base64.is_null() { return; }
    let base64 = unsafe { Box::from_raw(base64) };
    drop(base64);
}

