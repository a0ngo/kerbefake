"""
We read the messages from the message.json file until we find messages for the codes 1027 (request symmetric key)
and 1603 (response for symmetric key)

We know the structure of the requests due to the protocol being known, so:
For 1027 we examine the first 23 bytes to see the header, followed by 8 byte which is the nonce.

For 1603 we examine the first 9 bytes to see the header, followed by 16 byte which are the header (which match the
header's client ID), following which we have an encrypted key and ticket. We know the expected size of the encrypted key
and the ticket, so we know what's the expected size (the closest 16 multiple that isn't the current value, meaning if
we have 16 bytes data we expect 32 bytes).

Once we identify the ticket, we can start the offline attack.
Since the ticket is encrypted with the SHA-256 of the user's hash (which is known because the protocol is known)
we can start iterating over all the passwords in known_passwords (taken from
https://github.com/danielmiessler/SecLists/blob/master/Passwords/Common-Credentials/10-million-password-list-top-100000.txt )
until we find a password which does not result in a failure for decryption.

If we find such a password, we compare the nonce value from the ticket to the nonce value in the request, if they match
- we found the correct password; otherwise we continue looking.

It's important to note that there are better programs out there that do this work, for example hashcat.
Those programs use the GPU instead of the CPU used here, and result in much faster computation and checks, and higher
total hashing ability (higher hashes per second, HPS, or kHPS, kilo-hashes per second).
"""
import json
import math
import struct
from collections import namedtuple
from hashlib import sha256

from Crypto.Cipher import AES

# Define variables
request1027 = response1603 = messages = None

# Define named tuples and pack formats
RequestHeader = namedtuple("RequestHeader", "client_id version code payload_size")
RequestPayload = namedtuple("RequestPayload", "server_id nonce")
request_header_pack_format = "<16sBHI"
request_payload_pack_format = "<16sQ"

ResponseHeader = namedtuple("ResponseHeader", "version code payload_size")
ResponsePayload = namedtuple("ResponsePayload", "client_id encrypted_key_bytes ticket_bytes")
EncryptedEncryptedKey = namedtuple("EncryptedEncryptedKey", "iv encrypted_data")
EncryptedKeyData = namedtuple("EncryptedKeyData", "nonce aes_key")
Ticket = namedtuple("Ticket", "version client_id server_id iv encrypted_data")
response_header_pack_format = "<BHI"
# 32 + 8 = 40 -> encrypted data size is 48 for encrypted key, total = 48 + 16 = 64
# 32 + 8 = 40 -> encrypted data size is 48 for ticket , total = 48 + 16 + 8 + 16 + 16 + 1 = 105
response_payload_pack_format = "<16s64s105s"
encrypted_encrypted_key_pack_format = "<16s48s"
encrypted_key_data_pack_format = "<Q32s"
encrypted_ticket_key_pack_format = "<B16s16sQ16s48s"

# Read all the messages from the messages file.
# messages: [{"src": str, "dst": str, "hex": str, "code": int}]
try:
    messages = json.loads('\n'.join(open('./messages.json', 'r').readlines()))
except:
    print("No messages file found, using built in values")
    # Hardcoded backup just in case messages is not used.
    request1027 = "b31fcf6b46a04e33bd30cf6fe8821ba61803041800000021da1d0e32944e64944c6f864aa6b7b4a593d801d1c6edaf"
    response1603 = "184306c90000001e409a09c4a24a6ba4f9fd452e2658b926a10ca17d5cc4f073801a46f783bdc91204d7c864b82906ffbf9e2e2a1cc339679742b865793c0517880be3beb6ba8ea05f683a1406301bf0f8203edc45b112001e409a09c4a24a6ba4f9fd452e2658b921da1d0e32944e64944c6f864aa6b7b462a970958e01000001140409d6b7e2bb69cf57d8c5f08e46e96acdf98ebb94d74ab90936de8a2bda927b5a3ee86cfb25b5ae970e07e6115ce4cbe2f57e9a0472302b63ae3a5ddf9300000000000000000000000000000000"

# Find the messages we're interested in
if messages is not None:
    for message in messages:
        if message["code"] == 1027:
            print("Found get symmetric key request.")
            request1027 = message["hex"]
        if message["code"] == 1603:
            print("Found get symmetric key response.")
            response1603 = message["hex"]

# Break down to body and header
request_bytes = bytes.fromhex(request1027)
request_header_bytes = request_bytes[0:23]
request_payload_bytes = request_bytes[23:]
response_bytes = bytes.fromhex(response1603)
response_header_bytes = response_bytes[0:7]
response_payload_bytes = response_bytes[7:]

# Decode the messages, first the request header
request_header = RequestHeader._make(struct.unpack(request_header_pack_format, request_header_bytes))

if request_header.version != 24:
    print("Error - request version is not 24")
    exit(-1)
if request_header.code != 1027:
    print("Error - request code is not 1027")
    exit(-1)
if request_header.payload_size != len(request_payload_bytes):
    print("Error - request payload size does not match found payload size after removing the header")
    exit(-1)

# So far the request header is correct, we can proceed and find the nonce
request_payload = RequestPayload._make(struct.unpack(request_payload_pack_format, request_payload_bytes))
cleartext_nonce = request_payload.nonce

print(f"Got the nonce ({cleartext_nonce}), proceeding to unpack response.")

response_header = ResponseHeader._make(struct.unpack(response_header_pack_format, response_header_bytes))
if response_header.version != 24:
    print("Error - response header version is not 24")
    exit(-1)
if response_header.code != 1603:
    print("Error - response header code is not 1603")
    exit(-1)
if response_header.payload_size != len(response_payload_bytes):
    print("Error - response header payload size does not match found payload size ater removing the header")
    exit(-1)

# So far the header checks out
response_payload = ResponsePayload._make(struct.unpack(response_payload_pack_format, response_payload_bytes))
if response_payload.client_id != request_header.client_id:
    print("Error - response payload client id does not match request header client id")
    exit(-1)

encrypted_key = EncryptedEncryptedKey._make(
    struct.unpack(encrypted_encrypted_key_pack_format, response_payload.encrypted_key_bytes))
iv = encrypted_key.iv
encrypted_data = encrypted_key.encrypted_data

# Got all that's needed, starting offline attack.
print("Got all the needed information, starting offline attack.")
with open("./known_passwords.txt", "r") as password_file:
    while True:
        password = password_file.readline().strip()

        if not password:
            print("Finished running offline attack, didn't find password match")
            exit(-1)

        ciphertext_blocks = [encrypted_data[i:i + 16] for i in range(math.floor(len(encrypted_data) / 16))]
        password_hash = sha256(password.encode()).digest()

        # CBC Mode is known due to procotol being known
        cipher = AES.new(password_hash, AES.MODE_CBC, iv)
        try:
            decrypted = cipher.decrypt(encrypted_data)
            decrypted = decrypted[:-ord(decrypted[len(decrypted) - 1:])]

            # We decrypted, let's try to get the nonce
            encrypted_key_data = EncryptedKeyData._make(struct.unpack(encrypted_key_data_pack_format, decrypted))

            if cleartext_nonce == encrypted_key_data.nonce:
                print(f"Found password! {password}")
                break

        except:
            continue
