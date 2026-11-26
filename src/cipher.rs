use reqrio::*;
use std::{mem, slice};
use crate::HlsResult;

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Cipher_new(ct: CipherType) -> *mut Cipher {
    Box::into_raw(Box::new(Cipher::new(ct)))
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Cipher_set_secret_key(cipher: *mut Cipher, key: *const u8, key_len: usize, iv: *const u8, iv_len: usize) -> i32 {
    || -> HlsResult<i32>{
        let cipher = unsafe { cipher.as_mut() }.ok_or(RlsError::NullPtr)?;
        let key = unsafe { slice::from_raw_parts(key, key_len) };
        let iv = match iv.is_null() {
            true => None,
            false => Some(unsafe { slice::from_raw_parts(iv, iv_len) }),
        };
        cipher.set_secret_key(key, iv);
        Ok(0)
    }().unwrap_or(-1)
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Cipher_encrypt(cipher: *mut Cipher, ct: *const u8, ct_len: usize, out: *mut *mut u8, out_len: &mut usize) -> i32 {
    || -> HlsResult<i32>{
        let cipher = unsafe { cipher.as_mut() }.ok_or(RlsError::NullPtr)?;
        let data = unsafe { slice::from_raw_parts(ct, ct_len) };
        let mut en_bs = cipher.encrypt(data)?;
        unsafe {
            *out = en_bs.as_mut_ptr();
            *out_len = en_bs.len()
        };
        mem::forget(en_bs);
        Ok(0)
    }().unwrap_or(-1)
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Cipher_decrypt(cipher: *mut Cipher, ct: *const u8, ct_len: usize, out: *mut *mut u8, out_len: &mut usize) -> i32 {
    || -> HlsResult<i32>{
        let cipher = unsafe { cipher.as_mut() }.ok_or(RlsError::NullPtr)?;
        let data = unsafe { slice::from_raw_parts(ct, ct_len) };
        let mut en_bs = cipher.decrypt(data)?;
        unsafe {
            *out = en_bs.as_mut_ptr();
            *out_len = en_bs.len()
        };
        mem::forget(en_bs);
        Ok(0)
    }().unwrap_or(-1)
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Cipher_free(cipher: *mut Cipher) {
    if cipher.is_null() { return; }
    let cipher = unsafe { Box::from_raw(cipher) };
    drop(cipher);
}
