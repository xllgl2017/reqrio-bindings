//
// Created by XLX on 2026/11/14.
//

#ifndef REQRIO_QT_HTTP_EXAMPLE_H
#define REQRIO_QT_HTTP_EXAMPLE_H

#include "../Session.h"

void http_get_matrix(Session *session, const ALPN &alpn);

void http_method(const Session *session);

void http_status(const Session *session);

void http_auth(const Session *session);

void query_param(const Session *session);

void http_redirect(const Session *session);

void http_cookie(const Session *session);

void http_header(const Session *session);

void http_stream(const Session *session);

void http_body(const Session *session);

void http_concurrency(const Session *session);

#endif //REQRIO_QT_HTTP_EXAMPLE_H
