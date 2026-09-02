package main

/*
git tag reqrio-go/v0.3.0-rc3
git push origin reqrio-go/v0.3.0-rc3
*/
import (
	"encoding/hex"
	"fmt"
	"os"

	"github.com/xllgl2017/reqrio/reqrio-go/reqrio"
)

var headers = map[string]string{
	"User-Agent":                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
	"Accept":                    "*/*",
	"Sec-Fetch-Site":            "none",
	"Sec-Fetch-Mode":            "navigate",
	"Sec-Fetch-Dest":            "document",
	"sec-fetch-user":            "?1",
	"upgrade-insecure-requests": "1",
	"sec-ch-ua":                 "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Microsoft Edge\";v=\"120\"",
	"sec-ch-ua-mobile":          "?0",
	"sec-ch-ua-platform":        "\"Windows\"",
	"Accept-Language":           "zh-CN,zh;q=0.9",
	"Accept-Encoding":           "gzip,deflate,br,zstd",
	"Cache-Control":             "no-cache",
	"Connection":                "keep-alive",
}

func get() {
	session := reqrio.NewSession(false)
	err := session.SetHeaders(headers)
	if err != nil {
		panic(err)
	}
	err = session.Connect("https://www.baidu.com", "www.baidu.com")
	if err != nil {
		panic(err)
	}
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://www.baidu.com",
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
	cookies, err := resp.Cookies()
	if err != nil {
		panic(err)
	}
	fmt.Printf("%+v\n", cookies[0])
	text, err := resp.Text()
	if err != nil {
		panic(err)
	}
	fmt.Println("body len: ", len(text))
	resp, err = session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://www.baidu.com",
		Params: map[string]string{
			"v": "1",
			"b": "{'ddfd':34}",
		},
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())

}

func postForm() {
	session := reqrio.NewSession(false)
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.POST,
		Url:    "https://www.baidu.com",
		Data: map[string]string{
			"v": "1",
			"b": "{'ddfd':34}",
		},
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
}

func postJson() {
	session := reqrio.NewSession(false)
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.POST,
		Url:    "https://www.baidu.com",
		Json: map[string]any{
			"v": "1",
			"b": map[string]any{
				"ddfd": 34,
				"sdfg": false,
			},
		},
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
}

func postText() {
	session := reqrio.NewSession(false)
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method:      reqrio.POST,
		Url:         "https://www.baidu.com",
		Bytes:       []byte("hello world, text"),
		ContentType: "text/plain",
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
}

func postFiles() {
	session := reqrio.NewSession(false)
	err := session.SetKeyLog("../../2.log")
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.POST,
		Url:    "https://www.baidu.com",
		Data: map[string]string{
			"v": "1",
			"b": "{'ddfd':34}",
		},
		// Files: []reqrio.HttpFile{
		// 	reqrio.HttpFile{
		// 		Path:      "../../2.log",
		// 		FieldName: "file1",
		// 	},
		// 	reqrio.HttpFile{
		// 		Path:      "2.log",
		// 		FieldName: "file2",
		// 	},
		// },
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
}

func readToken() string {
	token, err := os.ReadFile("../../TOKEN")
	if err != nil {
		return ""
	}
	return string(token)
}

func clientHello() {
	//16030106b2
	var client_hello = "010006ae0303f0aed3d4d9fac0e8d4ff98981a90257765d203b4ce089c591e86d8e7ec8ab90a204803c2150a14429bfe6536328fe11cfd4034264fa2a3a443c5972eeeb93d427100206a6a130113021303c02bc02fc02cc030cca9cca8c013c014009c009d002f0035010006453a3a000000230000001b0003020002ff010001000000000e000c0000093338686d7a672e636e0005000501000000000017000044cd00050003026832fe0d00ba00000100010900208e3fc249e1ce71ff4aefb0970b38167b6b7de98537b874130ba4e284e15f1c4f00909540fc3a77fcc8f96d51ff9144785ccf114d3618d9a77b0e88f54d4dd1279083483e0ad83a4f25e55951194048709bf0842651d940c291569b9cfe1323d6fc2d31348ccaaa7b79271fc41af0975d94f7a826819154e05f6f90bdaa4e2b215894ccd36f748ded2bcae0a61aa101a7187588c2b45b51d076356d0e47728974d6d1cdd2b3ce4a8e5e8f70a79fb8f288c868000b00020100002d00020101000a000c000a3a3a11ec001d00170018001200000010000e000c02683208687474702f312e31003304ef04ed3a3a00010011ec04c05b20439ba8b50e3a5800981889512ab253cd2f1ba1488613fbd79f43813c08e34ed45330a62991a6b37890d54d2d0c089251b146acace84512c031c74ac6a2ac6345b6668629aa143357b45921916de02ac5cc8d57e1ca9882ccad900640a1b51c587de3291a2f15ad67e180b79b442fe4606de978f7a27591a41ffcd91116c50703c45531999c9d377a173c249ef747a60a81158c0d3ef709b9b5a38af61b6b5c9740c343f7322b6510a60797cb39148ba310413b688354bb0b2e395dbf3935fd0a797d7b5e94acab23a95c163238dd1bc9b8b420599a0efd4726e85a0783fc8506436c3eb89ee96008b0c9c5a2047a2415bbb5a2768d7c8d58384644d5473de96721b24a3fc82ee68cc0a3a43cc73467ec515a3ac1a79b9070f4e4aad61ac50c7b4e9b125f66cba026807cdad5a43e4a5cfa2ac521801616bb58ea068689c15afd4592b26545c3a8c638800a3429c32237a902f1a605458935391c4d352a211cb2122203f9ea38e3d44b29741502bb57c7850ffaf36ab0db72ac9c0fc0ba309661096bc550d86b442beca080c0602e02a54ed2171e58b0b82582c568a5b1407d8d35448cf907a43575aed4c5371595d1456f29778c892325d4d785a3a384a30b838e6b0d59990ca54ba52369c4faf835a2f50cbd504f7d38cdc4047bf7acae92090cf121180096a513dc4cadf290641ab6e4375aa477395b8902b74c39e62b945a09438d83b1d41ac2f204c4614425bed86e221c60c8520e1c3233e5ccb53c228c0d525fb7823d9d9c4337e36785eb61590794f9565b3dd2722a2834b536be157a307d928d7f910167a314b8705bdddc1b4c9c139a5320380910b1263b40a6c6065c84266a2c036a19d3a51f5edbb8eaf3cb1e8295ef1ab978f5306da9b11a5a3df473bbd2acca084a4c4bba0bc478630283b0e6910bde3052c6f58300703a6e9524381b4cc1b247236acc1c0bae6cb69c463c29811b04d93a589ba36d30c9b4d1fb234368a9b3e94abaf419a220af730917488bc9be585f7111c9a13a8544969bf3e397b1f2ceba0ca7f21785531a3f7856248f54a5bd854124b21e1e75c366e8b5293130bdb902db0a05e9803c3d7827d5cc26046815102c3713b4a14ef63aed3163319244995a6524dbabfaf93ed8a95e08641377683dd6b3b05084bf48f77d47904d09656d4a19b457d84bcfd77a4c433393bbb43f09931cf4896cf891990c9363202467b6193ea6b8bd493733235c93c118feb808b1d9b38cc7862c744342e2baeeec6299d0a21898aa9576ae61b2703a5b072521166f6693aa4b5e6148ad4e7c21a21a7972a0c8c3f986e95392ed2b15e51a5f2e5b90e4766320513e3bfa4d67688fb6c547147c47aa71c04095336b11b32b52a6c9d047a1357eece2688efb2045184653a480ef15a3fb8c4851d8c0407b24a87b55fd36af59b18fff38b183b6256e15c161395a46f62ce1b0af240319dec84d3aa04e2773ac289b393160683e901b2b622d615b2719b06cc12bae79fca101e737a91434c8e0828cc6a71b740216964a06a9952d9c54f24743b1b9c4fc9475554aa8a87719ccd7ae40374c87d8018937c7b6007e028b348e884d201087416396ec3237b61319e0f40e436a6a1dc75f2486a68c60c27f719d251a9d73b3de3bd91858d3f3d4043384f7ad42422b47b96bdd03b5556f8107232953dad801970157aa95971638e2908d55001d0020552cb65392fdab1ff61dd3b43c895fdf782c61bb6f05519f2b7d9e28facfd25e000d0012001004030804040105030805050108060601002b000706dada030403031a1a000100"
	bytes, err := hex.DecodeString(client_hello)
	if err != nil {
		panic(err)
	}
	fingerprint, err := reqrio.FromClientHello(bytes, readToken())
	if err != nil {
		panic(err)
	}
	session := reqrio.NewSession(false)
	err = session.SetFingerprint(fingerprint)
	if err != nil {
		panic(err)
	}
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://www.baidu.com",
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
}

func ja3() {
	var ja3Str = "771,4865-4866-4867-49195-49199-49196-49200-52393-52392-49171-49172-156-157-47-53,0-23-65281-10-11-35-16-5-13-18-51-45-43-27-17513-21,29-23-24,0"
	fingerprint, err := reqrio.FromJa3(ja3Str, readToken())
	if err != nil {
		panic(err)
	}
	session := reqrio.NewSession(false)
	err = session.SetFingerprint(fingerprint)
	if err != nil {
		panic(err)
	}
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://www.baidu.com",
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
}

func ja4() {
	var ja4Str = "t13d1516h2_002f,0035,009c,009d,1301,1302,1303,c013,c014,c02b,c02c,c02f,c030,cca8,cca9_0005,000a,000b,000d,0012,0017,001b,0023,002b,002d,0033,44cd,fe0d,ff01_0403,0804,0401,0503,0805,0501,0806,0601"
	fingerprint, err := reqrio.FromJa4(ja4Str, readToken())
	if err != nil {
		panic(err)
	}
	session := reqrio.NewSession(false)
	err = session.SetFingerprint(fingerprint)
	if err != nil {
		panic(err)
	}
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://www.baidu.com",
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
}

func randTls() {
	fingerprint, err := reqrio.RandomFingerprint(readToken())
	if err != nil {
		panic(err)
	}
	session := reqrio.NewSession(false)
	err = session.SetFingerprint(fingerprint)
	if err != nil {
		panic(err)
	}
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://www.baidu.com",
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())
}

func customTls() {
	fingerprint := reqrio.NewFingerprint(readToken())
	fingerprint.AddCipherSuites([]reqrio.CipherSuite{
		reqrio.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
		reqrio.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
		reqrio.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
		reqrio.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
		reqrio.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
		reqrio.TLS_AES_128_GCM_SHA256,
		reqrio.TLS_AES_256_GCM_SHA384,
		reqrio.TLS_CHACHA20_POLY1305_SHA256,
	})
	fingerprint.AddExtensionDefault(reqrio.StatusRequest)
	fingerprint.AddExtGroups(reqrio.SupportedGroup, []reqrio.SupportGroup{
		reqrio.X25519,
		reqrio.Secp256r1,
		reqrio.Secp384r1,
		reqrio.Secp521r1,
	})
	fingerprint.AddExtEcPoint(reqrio.EcPointFormats, []reqrio.EcPointFormat{
		reqrio.UNCOMPRESSED,
	})
	fingerprint.AddExtAlgorithm(reqrio.SignatureAlgorithms, []reqrio.Algorithm{
		reqrio.RSA_PKCS1_SHA1,
		reqrio.RSA_PKCS1_SHA256,
		reqrio.RSA_PKCS1_SHA384,
		reqrio.RSA_PKCS1_SHA512,
		reqrio.ECDSA_SECP256R1_SHA256,
		reqrio.ECDSA_SECP384R1_SHA384,
		reqrio.ECDSA_SECP521R1_SHA512,
		reqrio.RSA_PSS_PSS_SHA256,
		reqrio.RSA_PSS_PSS_SHA384,
		reqrio.RSA_PSS_PSS_SHA512,
		reqrio.RSA_PSS_RSAE_SHA256,
		reqrio.RSA_PSS_RSAE_SHA384,
		reqrio.RSA_PSS_RSAE_SHA512,
	})
	fingerprint.AddExtensionDefault(reqrio.SignedCertificateTimestamp)
	fingerprint.AddExtensionDefault(reqrio.ExtendMasterSecret)
	fingerprint.AddExtCompresses(reqrio.CompressionCertificate, []reqrio.CompressionMethod{
		reqrio.NULL,
	})
	fingerprint.AddExtensionDefault(reqrio.SessionTicket)
	fingerprint.AddExtVersions(reqrio.SupportedVersions, []reqrio.Version{
		0xeaea,
		reqrio.TLS_1_3,
		reqrio.TLS_1_2,
	})
	fingerprint.AddExtensionDefault(reqrio.PskKeyExchangeMode)
	fingerprint.AddExtGroups(reqrio.KeyShare, []reqrio.SupportGroup{
		reqrio.X25519,
		reqrio.Secp256r1,
	})
	fingerprint.AddExtAlps(reqrio.ApplicationSetting, []reqrio.ALPN{
		reqrio.HTTP20,
		reqrio.HTTP11,
	})
	fingerprint.AddExtensionDefault(reqrio.ServerName)
	fingerprint.AddExtAlps(reqrio.ApplicationLayerProtocolNegotiation, []reqrio.ALPN{
		reqrio.HTTP20,
		reqrio.HTTP11,
	})
	fingerprint.AddH2Setting(reqrio.HeaderTableSize, 65536)
	fingerprint.AddH2Setting(reqrio.EnablePush, 0)
	fingerprint.AddH2Setting(reqrio.InitialWindowSize, 6291456)
	fingerprint.AddH2Setting(reqrio.MaxHeaderListSize, 242144)
	fingerprint.SetH2WindowSize(2147418112)
	fingerprint.SetH2Priority(true, 147)
	session := reqrio.NewSession(false)
	err := session.SetFingerprint(fingerprint)
	if err != nil {
		panic(err)
	}
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://www.baidu.com",
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	fmt.Println(resp.StatusCode())

}

func flow_reader() {
	session := reqrio.NewSession(false)
	err := session.SetHeaders(headers)
	if err != nil {
		panic(err)
	}
	resp, err := session.SendRequest(reqrio.ConnParam{
		Method: reqrio.GET,
		Url:    "https://www.baidu.com",
		Stream: true,
	})
	if err != nil {
		panic(err)
	}
	defer resp.Delete()
	for chunk := range resp.Chunks() {
		println("chunk len: ", len(chunk))
	}
	if resp.Err() != nil {
		panic(resp.Err())
	}

}
func main() {
	get()
	postForm()
	postJson()
	postText()
	postFiles()
	clientHello()
	ja3()
	ja4()
	randTls()
	customTls()
	flow_reader()
}
