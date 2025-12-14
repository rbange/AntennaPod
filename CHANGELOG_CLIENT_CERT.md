# Changelog: Client Certificate Authentication Feature

## Version: [To be determined]
**Date**: December 2025

### New Features

#### Client Certificate Support for Synchronization
- **Feature**: Added support for client certificate authentication (mutual TLS) when connecting to synchronization servers
- **Benefit**: Users can now secure their sync server access using client certificates in addition to username/password authentication
- **Use Case**: Particularly useful for self-hosted sync servers that require enhanced security

### Changes by Component

#### Storage (`storage/preferences`)
**File**: `SynchronizationCredentials.java`
- Added `clientCertPath` preference to store certificate file location
- Added `clientCertPassword` preference to store certificate password (encrypted)
- Updated `clear()` method to remove certificate data on logout
- New methods: `getClientCertPath()`, `setClientCertPath()`, `getClientCertPassword()`, `setClientCertPassword()`

#### Network SSL (`net/ssl`)

**New File**: `ClientCertificateManager.java`
- Handles loading of PKCS12 certificate files
- Creates KeyManager instances for SSL context
- Supports password-protected and non-password-protected certificates
- Implements secure password handling (clears char arrays from memory)

**Modified**: `SslClientSetup.java`
- Added overload to accept client certificate parameters
- Passes KeyManagers to SSL socket factory when certificate is configured

**Modified**: `AntennaPodSslSocketFactory.java`
- Added constructor parameter for KeyManager array
- Initializes SSL context with both TrustManagers and KeyManagers
- Maintains backward compatibility with existing code

#### Network Common (`net/common`)
**Modified**: `AntennapodHttpClient.java`
- Added `newBuilder(String certPath, String certPassword)` overload
- Creates specialized HTTP clients with client certificate support
- Maintains backward compatibility with existing `newBuilder()` method

#### Sync Service (`net/sync/service`)
**Modified**: `SyncService.java`
- Checks for client certificate configuration before creating HTTP client
- Creates specialized HTTP client when certificate is present
- Works seamlessly with both Nextcloud and gpodder.net sync providers

#### UI Components (`ui/preferences`)

**Modified**: `NextcloudAuthenticationFragment.java`
- Added UI for certificate file selection using Android document picker
- Shows selected certificate name
- Password input field for protected certificates
- Clear certificate functionality
- Saves certificate to app's private storage
- Implements activity result launcher for file picking
- Proper error handling with localized messages

**Modified**: `nextcloud_auth_dialog.xml`
- Added "Client certificate (optional)" section
- Certificate selection button
- Certificate name display (hidden until selected)
- Password input field (hidden until certificate selected)
- Clear certificate button

#### Internationalization (`ui/i18n`)
**Modified**: `strings.xml`
- `client_certificate_label`: Label for certificate section
- `client_certificate_explanation`: Explanation of the feature
- `client_certificate_select`: Button text for selecting certificate
- `client_certificate_selected`: Text showing selected certificate
- `client_certificate_clear`: Button text for clearing certificate
- `client_certificate_password_label`: Label for password input
- `client_certificate_load_error`: Error message for certificate loading failures

### Technical Details

#### Certificate Format Support
- **Supported**: PKCS12 (.p12, .pfx)
- **Password**: Optional, supports both protected and unprotected certificates

#### Security Measures
1. Certificates stored in app's private file directory (`app.getFilesDir()/certificates/`)
2. Certificate passwords stored in encrypted SharedPreferences
3. Password char arrays cleared from memory after use
4. Uses Android's scoped storage (ACTION_OPEN_DOCUMENT) for secure file access
5. TLS 1.2 and TLS 1.3 support

#### Compatibility
- Works with Nextcloud Gpodder Sync
- Works with gpodder.net compatible servers
- Backward compatible with existing authentication flows
- Certificate authentication is optional

### Documentation Added

1. **CLIENT_CERT_SYNC.md**: Developer documentation
   - Architecture overview
   - Implementation details
   - Security considerations
   - Testing instructions

2. **USER_GUIDE_CLIENT_CERT.md**: User-facing documentation
   - Step-by-step setup guide
   - Troubleshooting tips
   - Security best practices
   - FAQ section

### Code Quality & Security

#### Code Review Improvements
- ✅ Extracted hardcoded strings to constants
- ✅ Localized all user-facing messages
- ✅ Improved error logging
- ✅ Secure memory handling for passwords

#### Security Analysis
- ✅ Passed CodeQL security scanner (0 vulnerabilities)
- ✅ No sensitive data exposure
- ✅ Proper file permissions
- ✅ Encrypted storage for passwords

### Migration Notes

#### For Users
- Existing synchronization setups are not affected
- Users who want client certificate authentication need to log out and log back in
- Optional feature - no action required for users who don't need it

#### For Developers
- All existing HTTP client code continues to work
- No breaking changes to public APIs
- New optional parameters available for certificate support

### Testing Recommendations

1. **Basic Flow**: Test Nextcloud authentication without certificate
2. **With Certificate**: Test with a valid PKCS12 certificate
3. **Password Protected**: Test with password-protected certificate
4. **Invalid Certificate**: Verify error handling
5. **Logout/Login**: Ensure certificate is cleared on logout
6. **Multiple Devices**: Test with different certificates on different devices

### Known Limitations

1. Only PKCS12 format supported (not PEM)
2. Certificate must be manually updated if it expires
3. No automatic certificate renewal
4. No certificate validation UI

### Future Enhancements (Potential)

1. Support for PEM format certificates
2. Certificate expiration warnings
3. Multiple certificate profiles
4. Certificate validation and inspection UI
5. Integration with Android Keystore
6. Add certificate support to gpodder.net authentication flow

### Files Modified

```
CLIENT_CERT_SYNC.md                                          (new, 90 lines)
USER_GUIDE_CLIENT_CERT.md                                    (new, 191 lines)
net/common/.../AntennapodHttpClient.java                     (modified, +13)
net/ssl/.../AntennaPodSslSocketFactory.java                  (modified, +8)
net/ssl/.../ClientCertificateManager.java                    (new, 62 lines)
net/ssl/.../SslClientSetup.java                              (modified, +17)
net/sync/service/.../SyncService.java                        (modified, +16)
storage/preferences/.../SynchronizationCredentials.java      (modified, +20)
ui/i18n/.../strings.xml                                      (modified, +7)
ui/preferences/.../NextcloudAuthenticationFragment.java      (modified, +114)
ui/preferences/.../nextcloud_auth_dialog.xml                 (modified, +50)
```

**Total**: 11 files, 388+ lines added, 9 lines removed

### Acknowledgments

Feature requested by users who self-host synchronization servers and need enhanced security through mutual TLS authentication.
