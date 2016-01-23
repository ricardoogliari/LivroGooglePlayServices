package com.example.ricardoogliari.livrogoogleplayservices;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient client;

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            //client.hasConnectedApi(Drive.API);
            client.connect();
        }
    }

    @Override
    protected void onStop() {
        client.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(getClass().getSimpleName(), "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(getClass().getSimpleName(), "onConnectionSuspend");
        switch (i){
            case GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST:
                //some type of feedback;
                break;
            case GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED:
                //some type of feedback;
                break;
            default:
                //some type of feedback;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        switch (connectionResult.getErrorCode()){
            case ConnectionResult.API_UNAVAILABLE:
                Log.e(getClass().getSimpleName(), "onConnectionFailed API_UNAVAILABLE");
                break;
            case ConnectionResult.CANCELED:
                Log.e(getClass().getSimpleName(), "onConnectionFailed CANCELED");
                break;
            case ConnectionResult.DEVELOPER_ERROR:
                Log.e(getClass().getSimpleName(), "onConnectionFailed DEVELOPER_ERROR");
                break;
            case ConnectionResult.INTERNAL_ERROR:
                Log.e(getClass().getSimpleName(), "onConnectionFailed INTERNAL_ERROR");
                break;
            case ConnectionResult.INTERRUPTED:
                Log.e(getClass().getSimpleName(), "onConnectionFailed INTERRUPTED");
                break;
            case ConnectionResult.INVALID_ACCOUNT:
                Log.e(getClass().getSimpleName(), "onConnectionFailed INVALID_ACCOUNT");
                break;
            case ConnectionResult.LICENSE_CHECK_FAILED:
                Log.e(getClass().getSimpleName(), "onConnectionFailed LICENSE_CHECK_FAILED");
                break;
            case ConnectionResult.NETWORK_ERROR:
                Log.e(getClass().getSimpleName(), "onConnectionFailed NETWORK_ERROR");
                break;
            case ConnectionResult.RESOLUTION_REQUIRED:
                Log.e(getClass().getSimpleName(), "onConnectionFailed RESOLUTION_REQUIRED");
                break;
            case ConnectionResult.RESTRICTED_PROFILE:
                Log.e(getClass().getSimpleName(), "onConnectionFailed RESTRICTED_PROFILE");
                break;
            case ConnectionResult.SERVICE_DISABLED:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SERVICE_DISABLED");
                break;
            case ConnectionResult.SERVICE_INVALID:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SERVICE_INVALID");
                break;
            case ConnectionResult.SERVICE_MISSING:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SERVICE_MISSING");
                break;
            case ConnectionResult.SERVICE_MISSING_PERMISSION:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SERVICE_MISSING_PERMISSION");
                break;
            case ConnectionResult.SERVICE_UPDATING:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SERVICE_UPDATING");
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SERVICE_VERSION_UPDATE_REQUIRED");
                break;
            case ConnectionResult.SIGN_IN_FAILED:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SIGN_IN_FAILED");
                break;
            case ConnectionResult.SIGN_IN_REQUIRED:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SIGN_IN_REQUIRED");
                break;
            case ConnectionResult.SUCCESS:
                Log.e(getClass().getSimpleName(), "onConnectionFailed SUCCESS");
                break;
            case ConnectionResult.TIMEOUT:
                Log.e(getClass().getSimpleName(), "onConnectionFailed TIMEOUT");
                break;
        }

        if (mResolvingError) {
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                client.connect();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    private void showErrorDialog(int errorCode) {
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }


    public void onDialogDismissed() {
        mResolvingError = false;
    }


    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                if (!client.isConnecting() &&
                        !client.isConnected()) {
                    client.connect();
                }
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }
}
