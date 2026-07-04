from reqrio import *

# 加解密测试
cipher = Cipher.aes_128_cbc("1234567812345678", "1234567812345678")
en_bs = cipher.encrypt('dada')
print(en_bs)
de_bs = cipher.decrypt(en_bs)
print(de_bs)

# 哈希测试
hash = Hasher(HashType.MD5)
hash.update(de_bs)
md5 = hash.finalize()
print(md5)

hmac = Hmac("keys", HashType.Sha1)
hmac.update(de_bs)
sha1_hmac = hmac.finalize()
print(sha1_hmac)

b64 = Base64()
b64_en = b64.encode(en_bs)
print(b64_en)
b64_de = b64.decode(b64_en)
print(b64_de)

ebs = en_b64(CipherType.AES_128_CBC, "wewrsfsdfsd", "1234567812345678", "1234567812345678")
print(ebs)

dbs = de_b64(CipherType.AES_128_CBC, ebs, "1234567812345678", "1234567812345678")
print(dbs)

ebs = en_hex(CipherType.AES_128_CBC, "wewrsfsdfsd", "1234567812345678", "1234567812345678")
print(ebs)

dbs = de_hex(CipherType.AES_128_CBC, ebs, "1234567812345678", "1234567812345678")
print(dbs)

uen = url_encode("https://122.233?")
print(uen)
dus = url_decode(uen)
print(dus)
