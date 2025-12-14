# UI Changes Summary: Client Certificate Authentication

## Overview
This document describes the user interface changes made to support client certificate authentication in the Nextcloud synchronization flow.

## Modified Screen: Nextcloud Authentication Dialog

### Location
**Settings → Synchronization → Choose synchronization provider → Nextcloud Gpodder Sync**

### New UI Elements

#### 1. Client Certificate Section
A new optional section has been added to the Nextcloud authentication dialog:

```
┌─────────────────────────────────────────┐
│  Nextcloud Server Configuration        │
├─────────────────────────────────────────┤
│                                         │
│  Server Address                         │
│  ┌───────────────────────────────────┐  │
│  │ https://cloud.example.com         │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ───────────────────────────────────    │
│                                         │
│  Client certificate (optional)          │
│  Select a PKCS12 (.p12 or .pfx)        │
│  certificate file for mutual TLS        │
│  authentication. This is optional...    │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │    [SELECT CERTIFICATE]           │  │
│  └───────────────────────────────────┘  │
│                                         │
│  [Proceed to login]                     │
│                                         │
└─────────────────────────────────────────┘
```

#### 2. After Certificate Selection
Once a certificate is selected, the UI updates to show:

```
┌─────────────────────────────────────────┐
│  Nextcloud Server Configuration        │
├─────────────────────────────────────────┤
│                                         │
│  Server Address                         │
│  ┌───────────────────────────────────┐  │
│  │ https://cloud.example.com         │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ───────────────────────────────────    │
│                                         │
│  Client certificate (optional)          │
│  Select a PKCS12 (.p12 or .pfx)        │
│  certificate file for mutual TLS        │
│  authentication. This is optional...    │
│                                         │
│  Certificate: client_cert.p12           │
│                                         │
│  Certificate password (if any)          │
│  ┌───────────────────────────────────┐  │
│  │ ●●●●●●●●                          │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │    [CLEAR CERTIFICATE]            │  │
│  └───────────────────────────────────┘  │
│                                         │
│  [Proceed to login]                     │
│                                         │
└─────────────────────────────────────────┘
```

### UI Component Details

#### Certificate Selection Button
- **Label**: "SELECT CERTIFICATE" (initially) / "CLEAR CERTIFICATE" (after selection)
- **Style**: Outlined Material Button
- **Action**: Opens Android's document picker filtered for PKCS12 files
- **Behavior**: Toggles between select and clear modes

#### Certificate Path Display
- **Visibility**: Hidden until certificate is selected
- **Content**: Shows the filename of the selected certificate
- **Format**: "Certificate: [filename]"
- **Style**: Small text (12sp)

#### Certificate Password Input
- **Visibility**: Hidden until certificate is selected
- **Type**: Password input (masked)
- **Hint**: "Certificate password (if any)"
- **Style**: Material Outlined TextInputLayout
- **IME Action**: Done
- **Optional**: Can be left empty for non-password-protected certificates

#### Explanation Text
- **Content**: Brief explanation of what client certificates are for
- **Style**: Small text (12sp)
- **Purpose**: Helps users understand the optional feature

### User Flow

#### Scenario 1: Authentication Without Certificate (Existing Flow)
1. User taps "Choose synchronization provider"
2. Selects "Nextcloud Gpodder Sync"
3. Enters server address
4. Taps "Proceed to login"
5. Completes browser-based authentication
6. ✅ **No changes to existing behavior**

#### Scenario 2: Authentication With Certificate (New Flow)
1. User taps "Choose synchronization provider"
2. Selects "Nextcloud Gpodder Sync"
3. Enters server address
4. Taps "SELECT CERTIFICATE"
5. Picks certificate file from device
6. (Optional) Enters certificate password
7. Taps "Proceed to login"
8. Completes browser-based authentication
9. ✅ **Certificate used for all sync requests**

#### Scenario 3: Changing Certificate
1. User is already logged in with a certificate
2. Taps "Logout" in Synchronization settings
3. Follows Scenario 2 with new certificate

#### Scenario 4: Removing Certificate
1. User is already logged in with a certificate
2. Taps "Logout" in Synchronization settings
3. Follows Scenario 1 (without selecting certificate)

### Visual Hierarchy

The certificate section is placed:
- **After**: Server address input
- **Before**: "Proceed to login" button
- **Spacing**: Adequate margins to separate from other sections
- **Emphasis**: Bold label to indicate new optional feature

### Accessibility Considerations

1. **Labels**: All inputs have proper labels for screen readers
2. **Hints**: Input fields have descriptive hints
3. **Feedback**: Success and error states are clearly communicated
4. **Touch Targets**: Buttons meet minimum size requirements (48dp)
5. **Contrast**: Text follows Material Design contrast guidelines

### Error Handling UI

When certificate loading fails:
```
┌─────────────────────────────────────────┐
│  Error                                  │
├─────────────────────────────────────────┤
│                                         │
│  Failed to load certificate:            │
│  Invalid PKCS12 format                  │
│                                         │
│            [OK]                         │
│                                         │
└─────────────────────────────────────────┘
```

### Internationalization

All UI text is externalized to `strings.xml`:
- `client_certificate_label`
- `client_certificate_explanation`
- `client_certificate_select`
- `client_certificate_selected`
- `client_certificate_clear`
- `client_certificate_password_label`
- `client_certificate_load_error`

Ready for translation to all supported languages.

## Implementation Details

### Layout File
**File**: `ui/preferences/src/main/res/layout/nextcloud_auth_dialog.xml`

**New Components**:
- TextView (label)
- TextView (explanation)
- Button (select/clear)
- TextView (certificate name)
- TextInputLayout (password)
- TextInputEditText (password input)

### Fragment Code
**File**: `NextcloudAuthenticationFragment.java`

**New Methods**:
- `openCertificatePicker()`: Launches file picker
- `handleCertificateSelected(Uri)`: Processes selected certificate
- `updateCertificateDisplay()`: Updates UI based on certificate state
- `clearCertificate()`: Removes selected certificate

**New Fields**:
- `selectedCertPath`: Stores path to selected certificate
- `certPickerLauncher`: ActivityResultLauncher for file picking
- `CERT_DIR_NAME`: Constant for certificate directory
- `CERT_FILE_NAME`: Constant for certificate filename

## Testing Checklist

- [ ] UI displays correctly on phones
- [ ] UI displays correctly on tablets
- [ ] Certificate selection opens file picker
- [ ] Selected certificate name appears
- [ ] Password field appears after selection
- [ ] Clear button removes certificate
- [ ] Error dialog shows on invalid certificate
- [ ] All text is properly localized
- [ ] Dark mode displays correctly
- [ ] Accessibility: Screen reader announces all elements
- [ ] Rotation preserves selected certificate

## Screenshots Needed

To complete this documentation, screenshots should be taken of:
1. Initial dialog (no certificate selected)
2. File picker dialog
3. Dialog after certificate selection
4. Error dialog for invalid certificate
5. Dark mode variants

## Related Files

- Layout: `nextcloud_auth_dialog.xml`
- Fragment: `NextcloudAuthenticationFragment.java`
- Strings: `strings.xml`
- Credentials: `SynchronizationCredentials.java`

## Backward Compatibility

✅ **Fully backward compatible**
- Existing users see no changes until they choose to use certificates
- Certificate selection is completely optional
- No changes to existing authentication flows
- No database migrations required
