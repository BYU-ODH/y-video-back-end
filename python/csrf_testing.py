import requests as r
import json

URL = "http://localhost:3030"

print("testing echo GET")
res = r.get(f"{URL}/api/echo?echo=Hello,+World!")
if res.status_code != 200:
    print("GET test failed")
    print(res.status_code)
    print(res.text)
    exit(1)

print("testing echo POST")
data = {"echo": "Hello, World!"}
res = r.post(f"{URL}/api/echo", json=data)
if res.status_code != 403:
    print("POST test failed")
    print(res.status_code)
    print(res.text)

print("testing echo PATCH")
data = {"first": "Hello, World!"}
res = r.patch(f"{URL}/api/echo", json=data)
if res.status_code != 403:
    print("PATCH test failed")
    print(res.status_code)
    print(res.text)


print("all tests passed")
