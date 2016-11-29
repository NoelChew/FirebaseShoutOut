package com.noelchew.firebaseshoutout.util.fcm;

import android.content.Context;
import android.util.Log;

import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.util.fcm.model.AppInstanceInfo;
import com.noelchew.firebaseshoutout.util.fcm.model.TopicHashmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by noelchew on 30/08/16.
 */
public class FcmUtils {
    private static final String TAG = "FPushNotificationUtils";

    public static void sendPushNotification(Context context, String fcmToken, String title, String message, final FcmCloudMessagingCallback fcmCloudMessagingCallback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        JSONObject messageJsonObject = new JSONObject();
        try {
            jsonObject.put("to", fcmToken);
            messageJsonObject.put("title", title);
            messageJsonObject.put("body", message);
            messageJsonObject.put("sound", "default");
            jsonObject.put("data", messageJsonObject);
            jsonObject.put("notification", messageJsonObject);
            jsonObject.put("priority", "high");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        final Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .addHeader("Authorization", "key=" + context.getString(R.string.fcm_server_key))
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Log.d(TAG, "OkHttp request " + request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e: " + e.getMessage());
                fcmCloudMessagingCallback.onPushFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "onResponse NOT SUCCESSFUL body: " + body);
                    fcmCloudMessagingCallback.onPushFailed(body);
                } else {
                    Log.d(TAG, "onResponse SUCCESS");
                    fcmCloudMessagingCallback.onPushSuccess();
                }
            }
        });
    }

    public static void sendTopicNotificationDataMessage(Context context, String topicId, String notificationTitle, String dataTitle, String message, final FcmCloudMessagingCallback fcmCloudMessagingCallback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject payload = new JSONObject();
        JSONObject notificationObject = new JSONObject();
        JSONObject dataObject = new JSONObject();
        try {
            payload.put("to", "/topics/" + topicId);

            notificationObject.put("title", notificationTitle);
            notificationObject.put("body", message);
            notificationObject.put("sound", "default");
            payload.put("notification", notificationObject);

            dataObject.put("topicId", topicId);
            dataObject.put("title", dataTitle);
            dataObject.put("body", message);
            dataObject.put("sound", "default");
            payload.put("data", dataObject);

            payload.put("priority", "high");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());
        final Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .addHeader("Authorization", "key=" + context.getString(R.string.fcm_server_key))
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Log.d(TAG, "OkHttp request " + request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e: " + e.getMessage());
                fcmCloudMessagingCallback.onPushFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "onResponse NOT SUCCESSFUL body: " + body);
                    fcmCloudMessagingCallback.onPushFailed(body);
                } else {
                    Log.d(TAG, "onResponse SUCCESS");
                    fcmCloudMessagingCallback.onPushSuccess();
                }
            }
        });
    }

    public static void sendTopicDataMessage(Context context, String topicId, String notificationTitle, String dataTitle, String message, final FcmCloudMessagingCallback fcmCloudMessagingCallback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject payload = new JSONObject();
        JSONObject dataObject = new JSONObject();
        try {
            payload.put("to", "/topics/" + topicId);

            dataObject.put("topicId", topicId);
            dataObject.put("title", dataTitle);
            dataObject.put("body", message);
            dataObject.put("sound", "default");
            payload.put("data", dataObject);

            payload.put("priority", "high");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());
        final Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .addHeader("Authorization", "key=" + context.getString(R.string.fcm_server_key))
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Log.d(TAG, "OkHttp request " + request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e: " + e.getMessage());
                fcmCloudMessagingCallback.onPushFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "onResponse NOT SUCCESSFUL body: " + body);
                    fcmCloudMessagingCallback.onPushFailed(body);
                } else {
                    Log.d(TAG, "onResponse SUCCESS");
                    fcmCloudMessagingCallback.onPushSuccess();
                }
            }
        });
    }

    public static void sendTopicNotificationMessage(Context context, String topic, String notificationTitle, String dataTitle, String message, final FcmCloudMessagingCallback fcmCloudMessagingCallback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject payload = new JSONObject();
        JSONObject notificationObject = new JSONObject();
        try {
            payload.put("to", "/topics/" + topic);

            notificationObject.put("title", notificationTitle);
            notificationObject.put("body", message);
            notificationObject.put("sound", "default");
            payload.put("notification", notificationObject);

            payload.put("priority", "high");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());
        final Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .addHeader("Authorization", "key=" + context.getString(R.string.fcm_server_key))
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Log.d(TAG, "OkHttp request " + request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e: " + e.getMessage());
                fcmCloudMessagingCallback.onPushFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "onResponse NOT SUCCESSFUL body: " + body);
                    fcmCloudMessagingCallback.onPushFailed(body);
                } else {
                    Log.d(TAG, "onResponse SUCCESS");
                    fcmCloudMessagingCallback.onPushSuccess();
                }
            }
        });
    }

    public interface FcmCloudMessagingCallback {
        void onPushSuccess();
        void onPushFailed(String errorMessage);
    }

    public static void getSubscribedTopics(Context context, String fcmToken, final GetTopicSubscriptionCallback callback) {

        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url("https://iid.googleapis.com/iid/info/" + fcmToken + "?details=true") // also known as Instance ID token (IID token)
                .addHeader("Authorization", "key=" + context.getString(R.string.fcm_server_key))
                .addHeader("Content-Type", "application/json")
                .build();

        Log.d(TAG, "OkHttp request " + request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure e: " + e.getMessage());
                callback.onGetFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "onResponse NOT SUCCESSFUL body: " + body);
                    callback.onGetFailed(body);
                } else {
                    Log.d(TAG, "onResponse SUCCESS");
                    ArrayList<TopicHashmap.FcmTopic> topicArrayList = AppInstanceInfo.fromJson(body).getRelationship().getTopicArrayList();
                    callback.onGetSuccess(topicArrayList);
                }
            }
        });
    }

    public interface GetTopicSubscriptionCallback {
        void onGetSuccess(ArrayList<TopicHashmap.FcmTopic> topics);
        void onGetFailed(String errorMessage);
    }
}
