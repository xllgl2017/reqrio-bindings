package reqrio

import "errors"

// Method represents an HTTP method.
type Method int

const (
	GET Method = iota
	POST
	PUT
	HEAD
	DELETE
	OPTIONS
	TRACE
	PATCH
	QUERY
)

// ParseStringMethod converts a string to a Method.
// method: HTTP method text.
func ParseStringMethod(method string) (Method, error) {
	switch method {
	case "GET":
		return GET, nil
	case "POST":
		return POST, nil
	case "PUT":
		return PUT, nil
	case "HEAD":
		return HEAD, nil
	case "DELETE":
		return DELETE, nil
	case "OPTIONS":
		return OPTIONS, nil
	case "TRACE":
		return TRACE, nil
	case "PATCH":
		return PATCH, nil
	case "QUERY":
		return QUERY, nil
	default:
		return GET, errors.New("invalid method")
	}
}
