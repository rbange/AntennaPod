package de.danoeh.antennapod.net.ssl;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509TrustManager;
import java.util.Arrays;

public class SslClientSetup {
    public static void installCertificates(OkHttpClient.Builder builder) {
        installCertificates(builder, null, null);
    }

    public static void installCertificates(OkHttpClient.Builder builder, 
                                          String clientCertPath, String clientCertPassword) {
        X509TrustManager trustManager = BackportTrustManager.create();
        KeyManager[] keyManagers = null;
        
        if (clientCertPath != null && !clientCertPath.isEmpty()) {
            keyManagers = ClientCertificateManager.loadClientCertificate(
                    clientCertPath, clientCertPassword);
        }
        
        builder.sslSocketFactory(
                new AntennaPodSslSocketFactory(trustManager, keyManagers), 
                trustManager);
        builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT));
    }
}
