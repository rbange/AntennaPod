# User Guide: Client Certificate Authentication for Synchronization

## What is Client Certificate Authentication?

Client certificate authentication (also known as mutual TLS or mTLS) is an additional security layer for connecting to your synchronization server. Instead of relying solely on username and password, your device presents a digital certificate to prove its identity.

## Why Use Client Certificates?

- **Enhanced Security**: Adds an extra layer of authentication beyond username/password
- **Better Access Control**: Your server admin can issue certificates only to trusted devices
- **Public Server Protection**: Makes it safer to expose your sync server to the internet
- **No Password Sharing**: Each device can have its own certificate instead of sharing passwords

## Prerequisites

1. A synchronization server (Nextcloud with Gpodder Sync app or gpodder.net compatible server)
2. Your server must be configured to accept or require client certificates
3. A client certificate file in PKCS12 format (.p12 or .pfx)
4. The certificate password (if your certificate is password-protected)

## Getting Your Certificate

### If Your Admin Provides It:
Your server administrator should provide you with:
- A .p12 or .pfx certificate file
- The certificate password (if applicable)

### If You Need to Create One:
Ask your server administrator to create one for you, or if you manage the server:

```bash
# Generate a private key and certificate
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# Convert to PKCS12 format
openssl pkcs12 -export -out client_cert.p12 -inkey key.pem -in cert.pem
```

## Setting Up in AntennaPod

### For Nextcloud Synchronization:

1. **Open AntennaPod Settings**
   - Tap the menu icon (☰)
   - Select "Settings"
   - Tap "Synchronization"

2. **Choose Synchronization Provider**
   - If not already set up, tap "Choose synchronization provider"
   - Select "Nextcloud Gpodder Sync"

3. **Enter Server Address**
   - Enter your Nextcloud server URL
   - Example: `https://cloud.example.com`

4. **Select Client Certificate** (Optional)
   - Tap "Select certificate" button
   - Browse to your certificate file (.p12 or .pfx)
   - Select the file
   - The certificate name will appear below the button

5. **Enter Certificate Password** (If Required)
   - If your certificate is password-protected, enter the password
   - Otherwise, leave this field empty

6. **Complete Authentication**
   - Tap "Proceed to login"
   - Follow the browser-based authentication flow
   - Grant AntennaPod access to your Nextcloud account

7. **Start Synchronizing**
   - Once authenticated, your subscriptions and playback positions will sync
   - All future sync requests will use your client certificate

## Changing or Removing Your Certificate

### To Change Your Certificate:
1. Go to Settings → Synchronization
2. Tap "Logout" to disconnect
3. Set up synchronization again with the new certificate

### To Remove Certificate Authentication:
1. Go to Settings → Synchronization  
2. Tap "Logout" to disconnect
3. Set up synchronization again without selecting a certificate

## Troubleshooting

### "Failed to load certificate"
**Possible causes:**
- The file is not a valid PKCS12 certificate
- The certificate password is incorrect
- The file is corrupted

**Solutions:**
- Verify you're using a .p12 or .pfx file
- Check the password with your administrator
- Try exporting the certificate again

### "Connection failed" or "Authentication error"
**Possible causes:**
- Your server doesn't accept the certificate
- The certificate has expired
- Server is not configured for client certificates

**Solutions:**
- Contact your server administrator
- Check certificate expiration date
- Verify server configuration

### Certificate Not Being Used
**Check:**
- Make sure you selected the certificate before tapping "Proceed to login"
- The certificate path should be displayed after selection
- Try removing and re-adding the certificate

## Security Best Practices

1. **Keep Your Certificate Safe**: Store your certificate file securely
2. **Don't Share**: Each device should have its own certificate
3. **Use Strong Passwords**: If your certificate is password-protected, use a strong password
4. **Monitor Expiration**: Check when your certificate expires and renew it in advance
5. **Revoke When Lost**: If you lose your device, ask your admin to revoke the certificate

## Frequently Asked Questions

**Q: Do I need a certificate to use synchronization?**  
A: No, certificates are optional. They provide additional security if your server supports or requires them.

**Q: Can I use the same certificate on multiple devices?**  
A: While technically possible, it's not recommended for security reasons. Each device should have its own certificate.

**Q: What happens if my certificate expires?**  
A: Synchronization will fail. You'll need to get a new certificate and set it up in AntennaPod.

**Q: Does this work with gpodder.net?**  
A: Yes, if your gpodder.net-compatible server is configured to accept client certificates.

**Q: Is my certificate password stored securely?**  
A: Yes, it's stored in Android's encrypted SharedPreferences.

## Need Help?

- Check the [AntennaPod Forum](https://forum.antennapod.org/)
- Report issues on [GitHub](https://github.com/AntennaPod/AntennaPod/issues)
- Contact your server administrator for certificate-specific issues
