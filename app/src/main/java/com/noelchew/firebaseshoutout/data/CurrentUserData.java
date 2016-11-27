package com.noelchew.firebaseshoutout.data;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.model.User;

/**
 * Created by noelchew on 26/11/2016.
 */

public class CurrentUserData {
    public static User getCurrentUser(Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User user;
        if (firebaseUser == null) {
            return null;
        } else if (!firebaseUser.isAnonymous()) {
            String profileImageUrl;
            if (firebaseUser.getPhotoUrl() != null) {
                profileImageUrl = firebaseUser.getPhotoUrl().toString();
            } else {
                profileImageUrl = "";
            }
            user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), FcmTokenData.getFcmToken(context), firebaseUser.getEmail(), profileImageUrl);
        } else {
            user = new User(firebaseUser.getUid(), context.getString(R.string.anonymous), FcmTokenData.getFcmToken(context), "", "");
        }

        user.setFcmUserDeviceId(FirebaseInstanceId.getInstance().getToken());
        return user;
    }
}
