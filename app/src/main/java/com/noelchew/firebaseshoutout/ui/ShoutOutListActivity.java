package com.noelchew.firebaseshoutout.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.joanzapata.iconify.widget.IconTextView;
import com.noelchew.firebaseshoutout.BuildConfig;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.helper.ShoutOutTopicHelper;
import com.noelchew.firebaseshoutout.model.NotificationEvent2;
import com.noelchew.firebaseshoutout.model.ShoutOut;
import com.noelchew.firebaseshoutout.model.ShoutOutTopic;
import com.noelchew.firebaseshoutout.model.User;
import com.noelchew.firebaseshoutout.ui.holder.ShoutOutHolder;
import com.noelchew.firebaseshoutout.ui.layoutmanager.NpaLinearLayoutManager;
import com.noelchew.firebaseshoutout.util.AnalyticsUtil;
import com.noelchew.firebaseshoutout.util.fcm.FcmUtils;
import com.noelchew.ncutils.ClipboardUtil;
import com.noelchew.ncutils.KeyboardUtil;
import com.noelchew.ncutils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by noelchew on 29/11/2016.
 */

public class ShoutOutListActivity extends AppCompatActivity {
    private static final String TAG = "ShoutOutListActivity";
    public static final String DATA_KEY = "ShoutOutTopicData";
    public static final String USER_KEY = "UserData";
    private static final int SHOUT_OUT_CHARACTER_LIMIT = 140;
    private static final long CACHE_EXPIRATION_PERIOD = 300; // in seconds

    Context context;

    CoordinatorLayout coordinatorLayout;
    RecyclerView recyclerView;
    LinearLayout llMakeShoutOut;
    EditText etShoutOut;
    IconTextView itvEmptyList, itvSend;
    ProgressDialog progressDialog;

    NpaLinearLayoutManager mLayoutManager;
    FirebaseRecyclerAdapter<ShoutOut, ShoutOutHolder> adapter;

    DatabaseReference shoutOutListDatabase;

    User user;
    ShoutOutTopic shoutOutTopic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shout_out_list);
        context = this;

        progressDialog = new ProgressDialog(ShoutOutListActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.loading);
        progressDialog.setMessage(getString(R.string.please_wait));

        AnalyticsUtil.sendAnalyticsScreenTrack(context, "ShoutOutListActivity");

        Bundle data = getIntent().getExtras();
        if (data == null) {
            ToastUtil.toastLongMessage(context, R.string.error_occurred);
            return;
        }
        shoutOutTopic = ShoutOutTopic.fromJson(data.getString(DATA_KEY));
        user = User.fromJson(data.getString(USER_KEY));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(shoutOutTopic.getTopicName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shoutOutListDatabase = FirebaseDatabase.getInstance().getReference(getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).child("shoutOuts");
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        llMakeShoutOut = (LinearLayout) findViewById(R.id.linear_layout_make_shout_out);
        itvEmptyList = (IconTextView) findViewById(R.id.icon_text_view_empty_list);
        etShoutOut = (EditText) findViewById(R.id.edit_text_shout_out);
        itvSend = (IconTextView) findViewById(R.id.icon_text_view_send);
        itvSend.setOnClickListener(itvSendOnClickListener);

        mLayoutManager = new NpaLinearLayoutManager(ShoutOutListActivity.this);
//        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

        if (shoutOutTopic.getUser().getId().equalsIgnoreCase(user.getId())) {
            // user owns the topic
            llMakeShoutOut.setVisibility(View.VISIBLE);
        } else {
            llMakeShoutOut.setVisibility(View.GONE);
        }
    }

    private void fetchRemoteConfig() {
        FirebaseRemoteConfig.getInstance().setConfigSettings(new FirebaseRemoteConfigSettings.Builder()
                // TODO: fix this part if necessary
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                // for demo purpose, enable developer mode
                .setDeveloperModeEnabled(true)
                .build());
        long cacheExpiry = CACHE_EXPIRATION_PERIOD;
        if (BuildConfig.DEBUG) {
            cacheExpiry = 0;
        }
        // TODO: fix this part if necessary
        // for demo purpose, set cacheExpiry to 0
        cacheExpiry = 0;
        FirebaseRemoteConfig.getInstance().setDefaults(R.xml.remote_config_defaults);
        FirebaseRemoteConfig.getInstance()
                .fetch(cacheExpiry)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Remote Config fetch success.");
                            FirebaseRemoteConfig.getInstance().activateFetched();
                            if (adapter != null) {
                                recyclerView.getRecycledViewPool().clear();
                                adapter.notifyDataSetChanged();
                            }

                        } else {
                            Log.d(TAG, "Remote Config fetch failed");
                            if (task.getException() != null) {
                                Log.d(TAG, "Error message: " + task.getException().getMessage());
//                                Log.d(TAG, "Remote config fetch failed - ${task.exception?.message}");
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        fetchRemoteConfig();
        shoutOutListDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //onDataChange called so remove progress bar
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                //make a call to dataSnapshot.hasChildren() and based
                //on returned value show/hide empty view
                if (dataSnapshot.hasChildren()) {
                    itvEmptyList.setVisibility(View.GONE);
                    Log.d(TAG, "dataSnapshot: " + dataSnapshot);
                } else {
                    itvEmptyList.setVisibility(View.VISIBLE);
                }

                //use helper method to add an Observer to RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        ToastUtil.toastLongMessage(context, R.string.error_occurred);
                    }
                });


            }
        });

//        Query topicQuery = shoutOutListDatabase.orderByChild("lastActiveDate/date");
        adapter = new FirebaseRecyclerAdapter<ShoutOut, ShoutOutHolder>(ShoutOut.class, R.layout.list_item_shout_out, ShoutOutHolder.class, shoutOutListDatabase) {
            @Override
            protected void populateViewHolder(ShoutOutHolder viewHolder, ShoutOut shoutOut, int position) {
                viewHolder.update(shoutOut, context, user, shoutOutHolderActionListener);
            }


        };
        adapter.registerAdapterDataObserver(mObserver);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (adapter != null)
            adapter.cleanup();
        super.onDestroy();
    }

    private ShoutOutHolder.ShoutOutHolderActionListener shoutOutHolderActionListener = new ShoutOutHolder.ShoutOutHolderActionListener() {
        @Override
        public void onLikeChanged(ShoutOut shoutOut, boolean toLike) {
            if (toLike) {
                AnalyticsUtil.sendAnalyticsEventTrack(context, "ShoutOut", "Like");
                ShoutOutTopicHelper.likeShoutOut(context, shoutOutTopic, shoutOut);
            } else {
                AnalyticsUtil.sendAnalyticsEventTrack(context, "ShoutOut", "Unlike");
                ShoutOutTopicHelper.unlikeShoutOut(context, shoutOutTopic, shoutOut);
            }
        }
    };

    RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            itvEmptyList.setVisibility(View.GONE);
            int totalCount = adapter.getItemCount();
            int lastVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();

            // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
            // to the bottom of the list to show the newly added message.
            if (lastVisiblePosition == -1 ||
                    (positionStart >= (totalCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                recyclerView.scrollToPosition(positionStart);
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (adapter.getItemCount() > 0) {
                itvEmptyList.setVisibility(View.GONE);
            } else {
                itvEmptyList.setVisibility(View.VISIBLE);
            }
        }
    };

    private View.OnClickListener itvSendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String message = etShoutOut.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                return;
            }

            if (message.length() > SHOUT_OUT_CHARACTER_LIMIT) {
                ClipboardUtil.copyText(context, message);
                ToastUtil.toastLongMessage(context, R.string.message_length_error);
                return;
            }

            ShoutOutTopicHelper.makeShoutOut(context, shoutOutTopic, message, new FcmUtils.FcmCloudMessagingCallback() {
                @Override
                public void onPushSuccess() {
                    AnalyticsUtil.sendAnalyticsEventTrack(context, "Shout Out", "Send Success");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.toastLongMessage(context, R.string.shout_out_success);
                        }
                    });
                }

                @Override
                public void onPushFailed(final String errorMessage) {
                    AnalyticsUtil.sendAnalyticsEventTrack(context, "Shout Out", "Send Failed: " + errorMessage);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.toastLongMessage(context, errorMessage);
                        }
                    });
                }
            });

            etShoutOut.setText("");
            KeyboardUtil.collapseKeyboard(ShoutOutListActivity.this);
        }
    };

    @Subscribe
    public void onEvent(final NotificationEvent2 event) {
        if (event.getTopicId().equalsIgnoreCase(shoutOutTopic.getTopicId())) {
            // do nothing

        } else {
            try {
                PugNotification.with(this)
                        .load()
                        .identifier(createNotificationId())
                        .title(event.getTopic())
                        .message(event.getMessage())
                        .bigTextStyle(event.getMessage())
                        .smallIcon(R.drawable.ic_bullhorn)
                        .largeIcon(R.mipmap.ic_launcher)
                        .flags(Notification.DEFAULT_ALL)
                        .click(createPendingIntent())
                        .autoCancel(true)
                        .simple()
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int createNotificationId() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("MMddHHmmss").format(now));
        return id;
    }
}
