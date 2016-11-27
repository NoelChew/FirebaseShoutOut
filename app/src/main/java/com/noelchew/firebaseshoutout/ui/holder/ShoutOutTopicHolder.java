package com.noelchew.firebaseshoutout.ui.holder;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.joanzapata.iconify.widget.IconTextView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.model.ShoutOutTopic;
import com.noelchew.firebaseshoutout.model.User;
import com.noelchew.ncutils.AlertDialogUtil;
import com.noelchew.ncutils.ResourceUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by noelchew on 26/11/2016.
 */

public class ShoutOutTopicHolder extends RecyclerView.ViewHolder {
    private static final String DATE_FORMAT = "h:mm:ss a d MMM yyyy";
    private static final String SHOW_TOPIC_CREATOR_KEY = "show_topic_creator";
    RelativeLayout rlHolder, rlCreator;
    TextView tvTopic, tvCreator, tvSubscriberCount, tvLastActiveDate, tvLastShoutOut;
    CheckBox cbSubscription;
    IconTextView itvAction;
    RoundedImageView rivCreator;

    Context mContext;
    ShoutOutTopic mShoutOutTopic;
    ShoutOutTopicHolderActionListener mListener;
    boolean mUserHasSubscribed;

    public ShoutOutTopicHolder(View itemView) {
        super(itemView);
        rlHolder = (RelativeLayout) itemView.findViewById(R.id.relative_layout);
        rlCreator = (RelativeLayout) itemView.findViewById(R.id.relative_layout_creator);
        tvTopic = (TextView) itemView.findViewById(R.id.text_view_topic);
        tvCreator = (TextView) itemView.findViewById(R.id.text_view_creator_name);
        tvSubscriberCount = (TextView) itemView.findViewById(R.id.text_view_subscriber_count);
        tvLastActiveDate = (TextView) itemView.findViewById(R.id.text_view_last_sent_message_date);
        tvLastShoutOut = (TextView) itemView.findViewById(R.id.text_view_last_sent_message);
        cbSubscription = (CheckBox) itemView.findViewById(R.id.check_box_subscription_status);
        itvAction = (IconTextView) itemView.findViewById(R.id.icon_text_view_action);
        rivCreator = (RoundedImageView) itemView.findViewById(R.id.image_view_creator);
    }

    public void update(ShoutOutTopic shoutOutTopic, Context context, User user, ShoutOutTopicHolderActionListener listener) {
        this.mContext = context;
        this.mShoutOutTopic = shoutOutTopic;
        this.mListener = listener;

        tvSubscriberCount.setText(context.getString(R.string.subscriber_count_label) + shoutOutTopic.getSubscriberCount());
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        tvLastActiveDate.setText(context.getString(R.string.last_active_label) + df.format(new Date(shoutOutTopic.getLastActiveDateInLong())));
        tvLastShoutOut.setText(context.getString(R.string.last_shout_out_label) + shoutOutTopic.getLastShoutOut());
        boolean isOwnTopic = false;
        if (shoutOutTopic.getUser().getId().equalsIgnoreCase(user.getId())) {
            isOwnTopic = true;
        }

        if (isOwnTopic) {
            tvTopic.setBackgroundColor(ResourceUtil.getColor(context, R.color.topic_background_own));
            tvTopic.setText(context.getString(R.string.is_own_topic_label) + ": " + shoutOutTopic.getTopicName());
            cbSubscription.setVisibility(View.GONE);
            itvAction.setVisibility(View.VISIBLE);
            itvAction.setOnClickListener(actionOnClickListener);
            rlHolder.setOnClickListener(actionOnClickListener);
        } else {
            tvTopic.setBackgroundColor(ResourceUtil.getColor(context, R.color.topic_background));
            tvTopic.setText(shoutOutTopic.getTopicName());
            cbSubscription.setVisibility(View.VISIBLE);
            ArrayList<User> subscribers = shoutOutTopic.getSubscriberArrayList();
            boolean userHasSubscribed = false;
            for (User subscriber : subscribers) {
                if (user.getId().equalsIgnoreCase(subscriber.getId())) {
                    userHasSubscribed = true;
                    break;
                }
            }
            this.mUserHasSubscribed = userHasSubscribed;

            cbSubscription.setChecked(userHasSubscribed);
            cbSubscription.setOnClickListener(subscriptionChangedOnClickListener);
            rlHolder.setOnClickListener(subscriptionChangedOnClickListener);
            itvAction.setVisibility(View.GONE);
        }

        if (FirebaseRemoteConfig.getInstance().getBoolean(SHOW_TOPIC_CREATOR_KEY)) {
            rlCreator.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(shoutOutTopic.getUser().getProfileImageUrl())) {
                Glide.with(context).load(shoutOutTopic.getUser().getProfileImageUrl()).into(rivCreator);
            } else {
                rivCreator.setImageResource(R.drawable.ic_person_black_24dp);
            }
            tvCreator.setText(shoutOutTopic.getUser().getName());
        } else {
            rlCreator.setVisibility(View.GONE);
        }

    }

    private View.OnClickListener actionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final ArrayList<String> selections = new ArrayList<>();
            selections.add(mContext.getString(R.string.action_send_shout_out));
            selections.add(mContext.getString(R.string.action_rename));
            selections.add(mContext.getString(R.string.action_delete));
            AlertDialogUtil.showAlertDialogWithSelections(mContext, selections, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // use string comparison so that the selections array at top can be swapped at will
                    if (selections.get(i).equalsIgnoreCase(mContext.getString(R.string.action_send_shout_out))) {
                        mListener.onSendShoutOut(mShoutOutTopic);
                    } else if (selections.get(i).equalsIgnoreCase(mContext.getString(R.string.action_rename))) {
                        mListener.onRenameTopic(mShoutOutTopic);
                    } else if (selections.get(i).equalsIgnoreCase(mContext.getString(R.string.action_delete))) {
                        mListener.onDeleteTopic(mShoutOutTopic);
                    }
                }
            });
        }
    };

    private View.OnClickListener subscriptionChangedOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onSubscriptionChanged(mShoutOutTopic, !mUserHasSubscribed);
        }
    };

    public interface ShoutOutTopicHolderActionListener {
        void onRenameTopic(ShoutOutTopic currentShoutOutTopic);

        void onDeleteTopic(ShoutOutTopic shoutOutTopic);

        void onSendShoutOut(ShoutOutTopic shoutOutTopic);

        void onSubscriptionChanged(ShoutOutTopic shoutOutTopic, boolean toSubscribe);
    }
}
