package com.noelchew.firebaseshoutout.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.joanzapata.iconify.widget.IconTextView;
import com.noelchew.firebaseshoutout.BuildConfig;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.data.FcmTokenData;
import com.noelchew.firebaseshoutout.data.SavedShoutOutData;
import com.noelchew.firebaseshoutout.helper.ShoutOutTopicHelper;
import com.noelchew.firebaseshoutout.model.NotificationEvent;
import com.noelchew.firebaseshoutout.model.ShoutOutTopic;
import com.noelchew.firebaseshoutout.model.User;
import com.noelchew.firebaseshoutout.ui.holder.ShoutOutTopicHolder;
import com.noelchew.firebaseshoutout.ui.layoutmanager.NpaLinearLayoutManager;
import com.noelchew.firebaseshoutout.util.AnalyticsUtil;
import com.noelchew.firebaseshoutout.util.fcm.FcmUtils;
import com.noelchew.ncutils.AlertDialogUtil;
import com.noelchew.ncutils.ClipboardUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int TOPIC_CHARACTER_LIMIT = 30;
    private static final int SHOUT_OUT_CHARACTER_LIMIT = 140;

    private static final long CACHE_EXPIRATION_PERIOD = 300; // in seconds

    Toolbar toolbar;
    ProgressDialog progressDialog;

    Context context;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;
    FirebaseRecyclerAdapter<ShoutOutTopic, ShoutOutTopicHolder> adapter;
    DatabaseReference userDatabase;
    DatabaseReference shoutOutTopicDatabase;

    User user;
    boolean userIsAnonymous = false;

    CoordinatorLayout coordinatorLayout;
    RelativeLayout rlContent;
    Button btnAddTopic;
    RecyclerView recyclerView;
    IconTextView itvEmptyList;

    NpaLinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.loading);
        progressDialog.setMessage(getString(R.string.please_wait));

        AnalyticsUtil.sendAnalyticsScreenTrack(context, "MainActivity");

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else if (!mFirebaseUser.isAnonymous()) {
            String profileImageUrl;
            if (mFirebaseUser.getPhotoUrl() != null) {
                profileImageUrl = mFirebaseUser.getPhotoUrl().toString();
            } else {
                profileImageUrl = "";
            }
            user = new User(mFirebaseUser.getUid(), mFirebaseUser.getDisplayName(), FcmTokenData.getFcmToken(context), mFirebaseUser.getEmail(), profileImageUrl);
        } else {
            userIsAnonymous = true;
            user = new User(mFirebaseUser.getUid(), getString(R.string.anonymous), FcmTokenData.getFcmToken(context), "", "");
        }

        user.setFcmUserDeviceId(FirebaseInstanceId.getInstance().getToken());

        userDatabase = FirebaseDatabase.getInstance().getReference(getString(R.string.users_node));
        shoutOutTopicDatabase = FirebaseDatabase.getInstance().getReference(getString(R.string.shout_out_topic_node));

        userDatabase.child(user.getId()).setValue(user);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        rlContent = (RelativeLayout) findViewById(R.id.relative_layout);
        itvEmptyList = (IconTextView) findViewById(R.id.icon_text_view_empty_list);

        btnAddTopic = (Button) findViewById(R.id.button_add_topic);
        btnAddTopic.setOnClickListener(btnAddTopicOnClickListener);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        // reverse order
        mLayoutManager = new NpaLinearLayoutManager(MainActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

        // get intent data
        Bundle data = getIntent().getExtras();
        if (data != null) {
//            long timestamp = data.getLong("google.sent_time");
//            Date sentDate = new Date(timestamp);
            String title = data.getString("title");
            String body = data.getString("body");

            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(body)) {
                SavedShoutOutData.appendSavedShoutOuts(context, new Date(), title, body);
            }
        }
        showShoutOutsAlertDialogIfNecessary();
    }

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
            boolean userHasCreatedTopic = false;
            for (int i = 0; i < adapter.getItemCount(); i++) {
                if (adapter.getItem(i).getUser().getId().equalsIgnoreCase(user.getId())) {
                    // user has created topic
                    userHasCreatedTopic = true;
                    break;
                }
            }
            if (userHasCreatedTopic) {
                btnAddTopic.setVisibility(View.GONE);
            } else {
                btnAddTopic.setVisibility(View.VISIBLE);
            }
        }
    };

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        fetchRemoteConfig();
        shoutOutTopicDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
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

                    // check whether user has created topic before
                    GenericTypeIndicator<HashMap<String, ShoutOutTopic>> t = new GenericTypeIndicator<HashMap<String, ShoutOutTopic>>() {
                    };

//                    List<String> yourStringArray = dataSnapshot.getValue(t);
                    HashMap<String, ShoutOutTopic> stringShoutOutTopicHashMap = dataSnapshot.getValue(t);
                    boolean userHasCreatedTopic = false;
                    Iterator it = stringShoutOutTopicHashMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        if (((ShoutOutTopic) pair.getValue()).getUser().getId().equalsIgnoreCase(user.getId())) {
                            userHasCreatedTopic = true;
                            break;
                        }
                    }

                    if (userHasCreatedTopic) {
                        btnAddTopic.setVisibility(View.GONE);
                    } else {
                        btnAddTopic.setVisibility(View.VISIBLE);
                    }
                } else {
                    itvEmptyList.setVisibility(View.VISIBLE);
                    btnAddTopic.setVisibility(View.VISIBLE);
                }

                //use helper method to add an Observer to RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Snackbar.make(coordinatorLayout, R.string.error_occurred, Snackbar.LENGTH_LONG).show();

            }
        });

        Query topicQuery = shoutOutTopicDatabase.orderByChild("lastActiveDate/date");
        adapter = new FirebaseRecyclerAdapter<ShoutOutTopic, ShoutOutTopicHolder>(ShoutOutTopic.class, R.layout.list_item_shout_out_topic, ShoutOutTopicHolder.class, topicQuery) {
            @Override
            protected void populateViewHolder(ShoutOutTopicHolder viewHolder, ShoutOutTopic shoutOutTopic, int position) {
                viewHolder.update(shoutOutTopic, context, user, shoutOutTopicHolderActionListenerListener);
            }
        };
        adapter.registerAdapterDataObserver(mObserver);
        recyclerView.setAdapter(adapter);
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

    private void logout() {
        AlertDialogUtil.showYesNoDialog(context, R.string.logout_confirmation_dialog_title, R.string.logout_confirmation_dialog_message, R.string.yes, R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                try {
                    unsubscribeAllSubscribedTopics();
//                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    AnalyticsUtil.sendAnalyticsEventTrack(context, "User", "Logout");
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finish();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Snackbar.make(coordinatorLayout, R.string.error_occurred, Snackbar.LENGTH_LONG).show();
//                }

            }
        }, null);
    }

    private void unsubscribeAllSubscribedTopics() {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ShoutOutTopicHelper.unsubscribeTopic(context, adapter.getItem(i));
        }
    }

    View.OnClickListener btnAddTopicOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialogUtil.showAlertDialogWithInput(context,
                    getString(R.string.add_shout_out_topic_dialog_title),
                    getString(R.string.add_shout_out_topic_dialog_message),
                    getString(R.string.add_shout_out_topic_dialog_input_hint),
                    user.getName(),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    new AlertDialogUtil.InputCallback() {
                        @Override
                        public void onInput(DialogInterface dialogInterface, CharSequence charSequence) {
                            if (charSequence == null || TextUtils.isEmpty(charSequence.toString().trim())) {
                                Snackbar.make(coordinatorLayout, R.string.empty_topic, Snackbar.LENGTH_LONG).show();
                                return;
                            } else if (charSequence.length() > TOPIC_CHARACTER_LIMIT) {
                                ClipboardUtil.copyText(context, charSequence.toString());
                                Snackbar.make(coordinatorLayout, R.string.add_shout_out_topic_length_error, Snackbar.LENGTH_LONG).show();
                                return;
                            }

                            ShoutOutTopicHelper.addShoutOutTopic(context, charSequence.toString().trim(), new ShoutOutTopicHelper.AddShoutOutTopicCallback() {
                                @Override
                                public void addSuccess() {
                                    AnalyticsUtil.sendAnalyticsEventTrack(context, "Topic", "Add Success");
                                    btnAddTopic.setVisibility(View.GONE);
                                    Snackbar.make(coordinatorLayout, R.string.add_shout_out_topic_success, Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void addFailed(String errorMessage) {
                                    AnalyticsUtil.sendAnalyticsEventTrack(context, "Topic", "Add Failed: " + errorMessage);
                                    Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    },
                    getString(R.string.add_shout_out_topic_positive_button),
                    getString(R.string.dialog_negative_button),
                    null);
        }
    };

    ShoutOutTopicHolder.ShoutOutTopicHolderActionListener shoutOutTopicHolderActionListenerListener = new ShoutOutTopicHolder.ShoutOutTopicHolderActionListener() {
        @Override
        public void onRenameTopic(final ShoutOutTopic currentShoutOutTopic) {
            AlertDialogUtil.showAlertDialogWithInput(context,
                    getString(R.string.rename_shout_out_topic_dialog_title),
                    getString(R.string.rename_shout_out_topic_dialog_message),
                    getString(R.string.rename_shout_out_topic_dialog_input_hint),
                    currentShoutOutTopic.getTopicName(),
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    new AlertDialogUtil.InputCallback() {
                        @Override
                        public void onInput(DialogInterface dialogInterface, CharSequence charSequence) {
                            if (charSequence == null || TextUtils.isEmpty(charSequence.toString().trim())) {
                                Snackbar.make(coordinatorLayout, R.string.empty_topic, Snackbar.LENGTH_LONG).show();
                                return;
                            } else if (charSequence.length() > TOPIC_CHARACTER_LIMIT) {
                                ClipboardUtil.copyText(context, charSequence.toString());
                                Snackbar.make(coordinatorLayout, R.string.add_shout_out_topic_length_error, Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            ShoutOutTopicHelper.renameShoutOutTopic(context, currentShoutOutTopic, charSequence.toString().trim(), new ShoutOutTopicHelper.RenameShoutOutTopicCallback() {
                                @Override
                                public void renameSuccess() {
                                    AnalyticsUtil.sendAnalyticsEventTrack(context, "Topic", "Rename Success");
                                    Snackbar.make(coordinatorLayout, R.string.rename_shout_out_topic_success, Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void renameFailed(String errorMessage) {
                                    AnalyticsUtil.sendAnalyticsEventTrack(context, "Topic", "Rename Failed: " + errorMessage);
                                    Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    },
                    getString(R.string.rename_shout_out_topic_dialog_positive_button),
                    getString(R.string.dialog_negative_button),
                    null);
        }

        @Override
        public void onDeleteTopic(final ShoutOutTopic shoutOutTopic) {
            AlertDialogUtil.showYesNoDialog(context, getString(R.string.delete_shout_out_topic_dialog_title), getString(R.string.delete_shout_out_topic_dialog_message), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ShoutOutTopicHelper.removeShoutOutTopic(context, shoutOutTopic, new ShoutOutTopicHelper.RemoveShoutOutTopicCallback() {
                        @Override
                        public void removeSuccess() {
                            AnalyticsUtil.sendAnalyticsEventTrack(context, "Topic", "Delete Success");
                            Snackbar.make(coordinatorLayout, R.string.delete_shout_out_topic_success, Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void removeFailed(String errorMessage) {
                            AnalyticsUtil.sendAnalyticsEventTrack(context, "Topic", "Delete Failed: " + errorMessage);
                            Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

        @Override
        public void onSendShoutOut(final ShoutOutTopic shoutOutTopic) {
            AlertDialogUtil.showAlertDialogWithInput(context,
                    shoutOutTopic.getTopicName(),
                    getString(R.string.shout_out_dialog_message),
                    getString(R.string.shout_out_dialog_input_hint),
                    "",
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
                    new AlertDialogUtil.InputCallback() {
                        @Override
                        public void onInput(DialogInterface dialogInterface, CharSequence charSequence) {
                            if (charSequence == null || TextUtils.isEmpty(charSequence.toString().trim())) {
                                Snackbar.make(coordinatorLayout, R.string.empty_message, Snackbar.LENGTH_LONG).show();
                                return;
                            } else if (charSequence.length() > SHOUT_OUT_CHARACTER_LIMIT) {
                                ClipboardUtil.copyText(context, charSequence.toString());
                                Snackbar.make(coordinatorLayout, R.string.message_length_error, Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            ShoutOutTopicHelper.makeShoutOut(context, shoutOutTopic, charSequence.toString().trim(), new FcmUtils.FcmCloudMessagingCallback() {
                                @Override
                                public void onPushSuccess() {
                                    AnalyticsUtil.sendAnalyticsEventTrack(context, "Shout Out", "Send Success");
                                    Snackbar.make(coordinatorLayout, R.string.shout_out_success, Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onPushFailed(String errorMessage) {
                                    AnalyticsUtil.sendAnalyticsEventTrack(context, "Shout Out", "Send Failed: " + errorMessage);
                                    Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    },
                    getString(R.string.shout_out_dialog_positive_button),
                    getString(R.string.dialog_negative_button),
                    null);
        }

        @Override
        public void onViewTopicShoutOuts(ShoutOutTopic shoutOutTopic) {
            Intent intent = new Intent(context, ShoutOutListActivity.class);
            intent.putExtra(ShoutOutListActivity.DATA_KEY, shoutOutTopic.toJson());
            intent.putExtra(ShoutOutListActivity.USER_KEY, user.toJson());
            startActivity(intent);
        }

        @Override
        public void onSubscriptionChanged(ShoutOutTopic shoutOutTopic, boolean toSubscribe) {
            if (toSubscribe) {
                AnalyticsUtil.sendAnalyticsEventTrack(context, "Topic", "Subscribe");
                ShoutOutTopicHelper.subscribeTopic(context, shoutOutTopic);
            } else {
                AnalyticsUtil.sendAnalyticsEventTrack(context, "Topic", "Unsubscribe");
                ShoutOutTopicHelper.unsubscribeTopic(context, shoutOutTopic);
            }
        }
    };

//    public static final String SAVED_MESSAGES = "savedMessages";
//    private static final String DATE_FORMAT = "h:mm:ss a d MMM yyyy";
//    private ArrayList<String> receivedMessages = new ArrayList<>();
    private AlertDialog alertDialog = null;

    @Subscribe
    public void onEvent(final NotificationEvent event) {
        showShoutOutsAlertDialogIfNecessary();
    }

    // WARNING: Note that using AlertDialog to show push messages is intrusive in terms of UX
    // this is used for demo purposes
    private void showShoutOutsAlertDialogIfNecessary() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String displayText = SavedShoutOutData.getSavedShoutOuts(context);
                if (TextUtils.isEmpty(displayText)) {
                    return;
                }
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.setOnDismissListener(null);
                    alertDialog.dismiss();
                }
//                String messageForDisplay = DateUtil.dateToString(new Date(), DATE_FORMAT) + "\n" +
//                        event.getTitle() + ": " + event.getBody();
//
//                receivedMessages.add(messageForDisplay);
//                messageForDisplay = "";
//                // iterate till second last
//                for (int i = 0; i < receivedMessages.size() - 1; i++) {
//                    messageForDisplay += receivedMessages.get(i) + "\n\n";
//                }
//
//                messageForDisplay += receivedMessages.get(receivedMessages.size() - 1);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.shout_out_data_dialog_title)
                        .setMessage(displayText)
                        .setPositiveButton(R.string.ok, null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                SavedShoutOutData.clearSavedShoutOuts(context);
                            }
                        });

                alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

}
