package com.noelchew.firebaseshoutout.ui.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.model.ShoutOut;
import com.noelchew.firebaseshoutout.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by noelchew on 26/11/2016.
 */

public class ShoutOutHolder extends RecyclerView.ViewHolder {
    private static final String DATE_FORMAT = "h:mm:ss a d MMM yyyy";
    private static final String SHOW_TOPIC_CREATOR_KEY = "show_topic_creator";
    RelativeLayout rlHolder;
    TextView tvDate, tvShoutOutMessage, tvLikeCount;
    CheckBox cbLike;

    Context mContext;
    ShoutOut mShoutOut;
    ShoutOutHolderActionListener mListener;
    boolean mUserHasLiked;

    public ShoutOutHolder(View itemView) {
        super(itemView);
        rlHolder = (RelativeLayout) itemView.findViewById(R.id.relative_layout);
        tvDate = (TextView) itemView.findViewById(R.id.text_view_date);
        tvShoutOutMessage = (TextView) itemView.findViewById(R.id.text_view_shout_out);
        tvLikeCount = (TextView) itemView.findViewById(R.id.text_view_like_count);
        cbLike = (CheckBox) itemView.findViewById(R.id.check_box_like_status);
    }

    public void update(ShoutOut shoutOut, Context context, User user, ShoutOutHolderActionListener listener) {
        this.mContext = context;
        this.mShoutOut = shoutOut;
        this.mListener = listener;

        tvLikeCount.setText(context.getString(R.string.like_count_label) + shoutOut.getLikeCount());
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        tvDate.setText(df.format(new Date(shoutOut.getDateCreatedInLong())));
        tvShoutOutMessage.setText(shoutOut.getMessage());

        ArrayList<User> likers = shoutOut.getLikeArrayList();
        boolean userHasLiked = false;
        for (User liker : likers) {
            if (user.getId().equalsIgnoreCase(liker.getId())) {
                userHasLiked = true;
                break;
            }
        }
        this.mUserHasLiked = userHasLiked;

        cbLike.setChecked(userHasLiked);
        cbLike.setOnClickListener(likeChangedOnClickListener);

    }

    private View.OnClickListener likeChangedOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onLikeChanged(mShoutOut, !mUserHasLiked);
        }
    };

    public interface ShoutOutHolderActionListener {
        void onLikeChanged(ShoutOut shoutOut, boolean toLike);
    }
}
