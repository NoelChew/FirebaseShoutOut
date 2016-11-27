package com.noelchew.firebaseshoutout.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.data.FcmTokenData;

/**
 * Created by noelchew on 27/08/2016.
 */
public class MyFcmInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFcmInstanceIDService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefreshed: " + refreshedToken);
        updateUserRegistrationToken(refreshedToken);
    }

    private void updateUserRegistrationToken(String token) {
        FcmTokenData.setFcmToken(getApplicationContext(), token);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference(getString(R.string.users_node))
                    .child(auth.getCurrentUser().getUid())
                    .child(getString(R.string.fcm_token_node))
                    .setValue(token);
        }
    }

}