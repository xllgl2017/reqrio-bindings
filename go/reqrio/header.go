package reqrio

type Cookie struct {
	Name     string `json:"name"`
	Value    string `json:"value"`
	Age      int    `json:"age"`
	Domain   string `json:"domain"`
	Path     string `json:"path"`
	HttpOnly bool   `json:"http_only"`
	Secure   bool   `json:"secure"`
	Expires  string `json:"expires"`
	Icpsp    bool   `json:"icpsp"`
	SameSite string `json:"same_site"`
}
