# Client Certificate Authentication for Synchronization

## Overview

This feature adds support for client certificate authentication (mutual TLS) when connecting to synchronization servers. This allows users to secure their sync server access using client certificates in addition to or instead of username/password authentication.

## How It Works

### For Users

1. When setting up Nextcloud synchronization, users can now optionally select a client certificate file.
2. The certificate should be in PKCS12 format (.p12 or .pfx file).
3. If the certificate is password-protected, users can provide the password.
4. The certificate is stored securely in the app's private storage.
5. All subsequent synchronization requests will use the client certificate for authentication.

### For Developers

The implementation consists of several key components:

#### 1. Storage (`SynchronizationCredentials`)
- Added `clientCertPath` and `clientCertPassword` preferences
- Stored alongside existing username/password credentials
- Cleared when user logs out

#### 2. SSL/TLS Configuration (`ClientCertificateManager`, `SslClientSetup`)
- `ClientCertificateManager`: Loads PKCS12 certificates and creates KeyManagers
- `SslClientSetup`: Modified to accept client certificate parameters
- `AntennaPodSslSocketFactory`: Updated to initialize SSL context with KeyManagers

#### 3. HTTP Client (`AntennapodHttpClient`)
- Added overload of `newBuilder()` that accepts certificate parameters
- Creates custom OkHttpClient instances with client certificate support

#### 4. Sync Service (`SyncService`)
- Modified to check for client certificate configuration
- Creates specialized HTTP client when certificate is present
- Works with both Nextcloud and gpodder.net sync providers

#### 5. UI (`NextcloudAuthenticationFragment`)
- Added certificate file picker using Android's document picker
- Shows certificate status and password input
- Copies selected certificate to app's private storage

## Security Considerations

1. **Certificate Storage**: Certificates are stored in the app's private file directory, which is only accessible to the app itself.
2. **Password Storage**: Certificate passwords are stored in encrypted SharedPreferences.
3. **File Access**: The app uses Android's scoped storage (ACTION_OPEN_DOCUMENT) to access certificates.
4. **TLS Version**: Supports TLS 1.2 and 1.3 for secure communication.

## Supported Certificate Formats

- PKCS12 (.p12, .pfx) - Recommended format
- Password-protected and non-password-protected certificates are both supported

## Testing

To test this feature:

1. Generate a test client certificate:
   ```bash
   openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes
   openssl pkcs12 -export -out client_cert.p12 -inkey key.pem -in cert.pem
   ```

2. Configure your sync server to require/accept client certificates

3. In AntennaPod:
   - Go to Settings → Synchronization
   - Choose a synchronization provider (e.g., Nextcloud)
   - Select the client certificate file
   - Enter password if required
   - Complete the authentication flow

## Limitations

- Only PKCS12 format is currently supported
- Certificate must be manually re-selected if it expires
- No automatic certificate renewal

## Future Enhancements

Potential improvements for future versions:

1. Support for PEM format certificates
2. Certificate expiration warnings
3. Multiple certificate profiles
4. Certificate validation UI
5. Integration with Android Keystore
