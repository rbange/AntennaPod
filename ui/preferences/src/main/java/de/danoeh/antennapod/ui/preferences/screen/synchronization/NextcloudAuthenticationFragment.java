package de.danoeh.antennapod.ui.preferences.screen.synchronization;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.DialogFragment;
import de.danoeh.antennapod.net.common.AntennapodHttpClient;
import de.danoeh.antennapod.net.sync.serviceinterface.SynchronizationProvider;
import de.danoeh.antennapod.net.sync.serviceinterface.SynchronizationQueue;
import de.danoeh.antennapod.storage.preferences.SynchronizationCredentials;
import de.danoeh.antennapod.storage.preferences.SynchronizationSettings;
import de.danoeh.antennapod.net.sync.nextcloud.NextcloudLoginFlow;
import de.danoeh.antennapod.ui.preferences.R;
import de.danoeh.antennapod.ui.preferences.databinding.NextcloudAuthDialogBinding;
import java.io.File;

/**
 * Guides the user through the authentication process.
 */
public class NextcloudAuthenticationFragment extends DialogFragment
        implements NextcloudLoginFlow.AuthenticationCallback {
    public static final String TAG = "NextcloudAuthenticationFragment";
    private static final String EXTRA_LOGIN_FLOW = "LoginFlow";
    private static final String EXTRA_CERT_PATH = "CertPath";
    private NextcloudAuthDialogBinding viewBinding;
    private NextcloudLoginFlow nextcloudLoginFlow;
    private boolean shouldDismiss = false;
    private String selectedCertPath = null;
    
    private final ActivityResultLauncher<Intent> certPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleCertificateSelected(uri);
                    }
                }
            });

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        dialog.setTitle(R.string.gpodnetauth_login_butLabel);
        dialog.setNegativeButton(R.string.cancel_label, null);
        dialog.setCancelable(false);
        this.setCancelable(false);

        viewBinding = NextcloudAuthDialogBinding.inflate(getLayoutInflater());
        dialog.setView(viewBinding.getRoot());

        viewBinding.selectCertButton.setOnClickListener(v -> openCertificatePicker());
        
        viewBinding.chooseHostButton.setOnClickListener(v -> {
            // Save certificate info before proceeding
            if (selectedCertPath != null) {
                SynchronizationCredentials.setClientCertPath(selectedCertPath);
                String certPassword = viewBinding.certPasswordText.getText() != null 
                    ? viewBinding.certPasswordText.getText().toString() 
                    : null;
                SynchronizationCredentials.setClientCertPassword(certPassword);
            }
            
            nextcloudLoginFlow = new NextcloudLoginFlow(AntennapodHttpClient.getHttpClient(),
                    viewBinding.serverUrlText.getText().toString(), getContext(), this);
            startLoginFlow();
        });
        
        if (savedInstanceState != null) {
            if (savedInstanceState.getStringArrayList(EXTRA_LOGIN_FLOW) != null) {
                nextcloudLoginFlow = NextcloudLoginFlow.fromInstanceState(AntennapodHttpClient.getHttpClient(),
                        getContext(), this, savedInstanceState.getStringArrayList(EXTRA_LOGIN_FLOW));
                startLoginFlow();
            }
            if (savedInstanceState.getString(EXTRA_CERT_PATH) != null) {
                selectedCertPath = savedInstanceState.getString(EXTRA_CERT_PATH);
                updateCertificateDisplay();
            }
        }
        return dialog.create();
    }
    
    private void openCertificatePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/x-pkcs12", "application/x-pem-file"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        certPickerLauncher.launch(intent);
    }
    
    private static final String CERT_DIR_NAME = "certificates";
    private static final String CERT_FILE_NAME = "client_cert.p12";
    
    private void handleCertificateSelected(Uri uri) {
        try {
            // Copy the certificate to app's private storage
            File certDir = new File(requireContext().getFilesDir(), CERT_DIR_NAME);
            if (!certDir.exists()) {
                certDir.mkdirs();
            }
            
            File destFile = new File(certDir, CERT_FILE_NAME);
            
            try (java.io.InputStream in = requireContext().getContentResolver().openInputStream(uri);
                 java.io.OutputStream out = new java.io.FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            
            selectedCertPath = destFile.getAbsolutePath();
            updateCertificateDisplay();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Failed to load certificate", e);
            final MaterialAlertDialogBuilder errorDialog = new MaterialAlertDialogBuilder(getContext());
            errorDialog.setTitle(R.string.error_label);
            errorDialog.setMessage(getString(R.string.client_certificate_load_error, e.getMessage()));
            errorDialog.setPositiveButton(android.R.string.ok, null);
            errorDialog.show();
        }
    }
    
    private void updateCertificateDisplay() {
        if (selectedCertPath != null) {
            viewBinding.certPathText.setVisibility(View.VISIBLE);
            viewBinding.certPathText.setText(getString(R.string.client_certificate_selected, 
                    new File(selectedCertPath).getName()));
            viewBinding.certPasswordTextInput.setVisibility(View.VISIBLE);
            viewBinding.selectCertButton.setText(R.string.client_certificate_clear);
            viewBinding.selectCertButton.setOnClickListener(v -> clearCertificate());
        } else {
            viewBinding.certPathText.setVisibility(View.GONE);
            viewBinding.certPasswordTextInput.setVisibility(View.GONE);
            viewBinding.selectCertButton.setText(R.string.client_certificate_select);
            viewBinding.selectCertButton.setOnClickListener(v -> openCertificatePicker());
        }
    }
    
    private void clearCertificate() {
        selectedCertPath = null;
        if (viewBinding.certPasswordText.getText() != null) {
            viewBinding.certPasswordText.getText().clear();
        }
        updateCertificateDisplay();
    }

    private void startLoginFlow() {
        viewBinding.chooseHostButton.setVisibility(View.GONE);
        viewBinding.loginProgressContainer.setVisibility(View.VISIBLE);
        viewBinding.serverUrlText.setEnabled(false);
        nextcloudLoginFlow.start();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nextcloudLoginFlow != null) {
            outState.putStringArrayList(EXTRA_LOGIN_FLOW, nextcloudLoginFlow.saveInstanceState());
        }
        if (selectedCertPath != null) {
            outState.putString(EXTRA_CERT_PATH, selectedCertPath);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (nextcloudLoginFlow != null) {
            nextcloudLoginFlow.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldDismiss) {
            dismiss();
        }
    }

    @Override
    public void onNextcloudAuthenticated(String server, String username, String password) {
        SynchronizationSettings.setSelectedSyncProvider(
                SynchronizationProvider.NEXTCLOUD_GPODDER.getIdentifier());
        SynchronizationCredentials.clear();
        SynchronizationQueue.getInstance().clear();
        SynchronizationCredentials.setPassword(password);
        SynchronizationCredentials.setHosturl(server);
        SynchronizationCredentials.setUsername(username);
        SynchronizationQueue.getInstance().fullSync();
        if (isResumed()) {
            dismiss();
        } else {
            shouldDismiss = true;
        }
    }

    @Override
    public void onNextcloudAuthError(String errorMessage) {
        viewBinding.loginProgressContainer.setVisibility(View.GONE);
        viewBinding.chooseHostButton.setVisibility(View.VISIBLE);
        viewBinding.serverUrlText.setEnabled(true);

        final MaterialAlertDialogBuilder errorDialog = new MaterialAlertDialogBuilder(getContext());
        errorDialog.setTitle(R.string.error_label);
        String genericMessage = getString(R.string.nextcloud_login_error_generic);
        SpannableString combinedMessage = new SpannableString(genericMessage + "\n\n" + errorMessage);
        combinedMessage.setSpan(new ForegroundColorSpan(0x88888888),
                genericMessage.length(), combinedMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        errorDialog.setMessage(combinedMessage);
        errorDialog.setPositiveButton(android.R.string.ok, null);
        errorDialog.show();
    }
}
