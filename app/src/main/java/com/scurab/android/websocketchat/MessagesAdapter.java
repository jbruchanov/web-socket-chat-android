package com.scurab.android.websocketchat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JBruchanov on 08/03/2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    final List<JSONObject> mMessages;

    public MessagesAdapter() {
        this(new ArrayList<>());
    }

    public MessagesAdapter(@NonNull List<JSONObject> data) {
        mMessages = data;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        JSONObject message = mMessages.get(position);
        try {
            holder.date.setText(message.getString("received"));
            holder.msg.setText(message.getString("data"));
            holder.user.setText(message.getString("from"));
        } catch (JSONException e) {
            throw new Error(e);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public void add(@NonNull JSONObject message) {
        mMessages.add(message);
        notifyItemInserted(getItemCount());
    }

    public void addAll(@NonNull JSONArray messages) {
        mMessages.clear();
        try {
            for (int i = 0, n = messages.length(); i < n; i++) {
                mMessages.add(messages.getJSONObject(i));
            }
            notifyDataSetChanged();
        } catch (JSONException e) {
            throw new Error(e);
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user) TextView user;
        @BindView(R.id.date) TextView date;
        @BindView(R.id.msg) TextView msg;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
