# Secure QR

Password-protected QR code scanner and generator using AES-256-GCM encryption.

---

## Google Play Store Listing

### Short Description

> Scan & create QR codes with AES-256 password encryption. Fully offline.

### Full Description

```
Secure QR lets you create and scan password-protected QR codes. All encryption and decryption happens on your device — your data is never sent to any server.

HOW IT WORKS
Secure QR encrypts your QR code content with a password using AES-256-GCM, a military-grade encryption standard. Only someone with the correct password can decrypt and read the original content.

1. Enter text or scan an existing QR code
2. Set a password and tap Encode to generate an encrypted QR code
3. Share the encrypted QR code image
4. The recipient scans it with Secure QR and enters the password to decrypt

FEATURES
• Scan QR codes instantly using your camera
• Generate standard QR codes from any text
• Encrypt QR code content with a password (AES-256-GCM)
• Decrypt password-protected QR codes
• Share generated QR code images with any app
• Works completely offline — no internet required for encryption or scanning

SECURITY
• AES-256-GCM encryption with authenticated encryption
• PBKDF2-HMAC-SHA256 key derivation (65,536 iterations)
• Random salt and IV generated for every encryption
• All cryptographic operations are performed locally on your device
• No data is collected or transmitted

PERFECT FOR
• Sharing Wi-Fi passwords securely
• Sending private links or messages via QR code
• Storing sensitive information in QR format
• Any situation where you need a QR code that only authorized people can read
```

---

## Technical Details

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 16)
- **Encryption**: AES-256-GCM with PBKDF2-HMAC-SHA256 key derivation
- **QR Scanning**: Google ML Kit Barcode Scanning
- **QR Generation**: ZXing
- **Architecture**: MVVM with AndroidX Navigation, CameraX, ViewBinding
