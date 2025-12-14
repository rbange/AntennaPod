package de.danoeh.antennapod.net.ssl;

import android.util.Log;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Manages client certificates for mutual TLS authentication.
 */
public class ClientCertificateManager {
    private static final String TAG = "ClientCertManager";

    /**
     * Load a client certificate from a file path.
     * Supports PKCS12 (.p12, .pfx) format.
     *
     * @param certPath Path to the certificate file
     * @param password Password for the certificate file (can be null or empty)
     * @return Array of KeyManagers or null if loading fails
     */
    public static KeyManager[] loadClientCertificate(String certPath, String password) {
        if (certPath == null || certPath.isEmpty()) {
            return null;
        }

        char[] passwordChars = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            passwordChars = (password != null && !password.isEmpty()) 
                ? password.toCharArray() 
                : new char[0];

            try (InputStream certStream = new FileInputStream(certPath)) {
                keyStore.load(certStream, passwordChars);
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, passwordChars);

            return keyManagerFactory.getKeyManagers();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException 
                | CertificateException | UnrecoverableKeyException e) {
            Log.e(TAG, "Failed to load client certificate from " + certPath, e);
            return null;
        } finally {
            // Clear password from memory for security
            if (passwordChars != null && passwordChars.length > 0) {
                java.util.Arrays.fill(passwordChars, ' ');
            }
        }
    }
}
