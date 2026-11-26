use crate::*;
use reqrio::*;
use std::ffi::{CStr, c_char};
use std::ptr::null_mut;
use std::slice;

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Body_new(data: *const u8, len: usize, ty: *const c_char, err: *mut *mut c_char) -> *mut Body<'static> {
    check_run(move || {
        let ty = unsafe { CStr::from_ptr(ty) }.to_str()?;
        let ty = ContentType::try_from(ty)?;
        let body = unsafe { slice::from_raw_parts(data, len) }.to_vec();
        let body = Body::from(body).with_ct(ty);
        Ok(Box::into_raw(Box::new(body)))
    }, |e| handle_err1(e, err, null_mut()))
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Body_none() -> *mut Body<'static> {
    let body: Body = None.into();
    Box::into_raw(Box::new(body))
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Body_new_files(file: *mut HttpFile, data: *const c_char, err: *mut *mut c_char) -> *mut Body<'static> {
    check_run(move || {
        let mut file = unsafe { Box::from_raw(file) };
        let data = json::from_bytes(unsafe { CStr::from_ptr(data) }.to_bytes())?;
        file.set_data(data);
        let body: Body = (*file).into();
        Ok(Box::into_raw(Box::new(body)))
    }, |e| handle_err1(e, err, null_mut()))
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn HttpFile_new() -> *mut HttpFile {
    Box::into_raw(Box::new(HttpFile::default()))
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn HttpFile_add_form(file: *mut HttpFile, form: *mut FileForm) -> *mut c_char {
    check_run(move || {
        let file = unsafe { file.as_mut() }.ok_or(HlsError::NullPointer)?;
        let form = unsafe { Box::from_raw(form) };
        file.add_form(*form);
        Ok(null_mut())
    }, handle_err2)
}


#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn FileForm_new(path: *const c_char, field_name: *const c_char, filetype: *const c_char, err: *mut *mut c_char) -> *mut FileForm {
    check_run(move || {
        let path = unsafe { CStr::from_ptr(path) }.to_str()?;
        let field_name = unsafe { CStr::from_ptr(field_name) }.to_str()?;
        let mut form = FileForm::new_path(path)?;
        form.set_field_name(field_name);
        if !filetype.is_null() {
            let file_type = unsafe { CStr::from_ptr(filetype) }.to_str()?;
            form.set_filetype(file_type);
        }
        Ok(Box::into_raw(Box::new(form)))
    }, |e| handle_err1(e, err, null_mut()))
}


#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn HttpFile_drop(form: *mut HttpFile) {
    if form.is_null() { return; }
    let form = unsafe { Box::from_raw(form) };
    drop(form);
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Body_drop(body: *mut Body) {
    if body.is_null() { return; }
    let body = unsafe { Box::from_raw(body) };
    drop(body);
}
