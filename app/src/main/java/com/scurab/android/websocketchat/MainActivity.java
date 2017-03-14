package com.scurab.android.websocketchat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.scurab.android.websocketchat.messaging.ChatService;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity {

    static final String MAIN_CHAT_TAG = "#";

    private ChatService mChatService;
    private String mUserName;

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    @BindView(android.R.id.tabhost)
    FragmentTabHost tabHost;

    @BindView(R.id.msg_edit_text)
    EditText msgEditText;

    @BindView(R.id.send_button)
    ImageButton mSendButton;

    @BindView(R.id.left_drawer)
    RecyclerView mUsers;

    private UsersAdapter mUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mChatService = onCreateChatService();
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        onAddTab(MAIN_CHAT_TAG, "MainChat");

        mUsers.setLayoutManager(new LinearLayoutManager(this));
        mUsers.setAdapter(mUsersAdapter = new UsersAdapter());
        mUsers.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .colorResId(R.color.colorPrimary)
                        .showLastDivider()
                        .build());
        mUsersAdapter.setOuterClickListener((user, i) -> {
            onAddTab(user, user);
            mDrawerLayout.closeDrawers();
            mDrawerLayout.post(() -> tabHost.setCurrentTabByTag(user));
        });

        mSendButton.setVisibility(View.INVISIBLE);
        mChatService.onConnectionChanged()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RxView.visibility(mSendButton, View.INVISIBLE));
    }

    @NonNull
    ChatService onCreateChatService() {
        return new ChatService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onShowUsernameDialog();
    }

    protected void onShowUsernameDialog() {
        if (mUserName == null || mUserName.length() < 2) {
            final EditText field = new EditText(this);
            field.setText("android-" + (1000 + new Random().nextInt(9000)));
            new AlertDialog.Builder(this)
                    .setView(field)
                    .setTitle(R.string.username)
                    .setCancelable(false)
                    .setPositiveButton(R.string.action_ok, (dialog, which) -> {
                        final String username = field.getText().toString();
                        if (username.length() >= 2) {
                            mUserName = username;
                            startService(username);
                        } else {
                            onShowUsernameDialog();
                        }
                    })
                    .show();
        } else {
            startService(mUserName);
        }
    }

    void startService(String username) {
        mChatService
                .start(username)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe((rawMessage) -> {
                    String type = rawMessage.getString("type");
                    if ("msg".equals(type)) {
                        String from = rawMessage.getString("from");
                        String to = rawMessage.optString("to");
                        onAddMessage(rawMessage, !TextUtils.isEmpty(to) ? from : "#");
                    } else if ("msgs".equals(type)) {
                        onLoadMessages(rawMessage.getJSONArray("data"));
                    } else if ("users".equals(type)) {
                        mUsersAdapter.setUsers(rawMessage.getJSONArray("data"));
                    }
                }, (err) -> {
                    Toast.makeText(MainActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
                    err.printStackTrace();
                });
    }

    public void onLoadMessages(JSONArray array) {
        MessagesFragment fragment = (MessagesFragment) getSupportFragmentManager().findFragmentByTag("#");
        fragment.addMessages(array);
    }

    public boolean onAddTab(@NonNull String tag, @NonNull String title) {
        return onAddTab(tag, title, null);
    }

    public boolean onAddTab(@NonNull String tag, @NonNull String title, @Nullable JSONObject msg) {
        boolean create = getSupportFragmentManager().findFragmentByTag(tag) == null;
        if (create) {
            Bundle bundle = null;
            if (msg != null) {
                bundle = new Bundle();
                bundle.putString("msg", msg.toString());
            }
            tabHost.addTab(tabHost.newTabSpec(tag).setIndicator(title), MessagesFragment.class, bundle);
        }
        return create;
    }

    @OnClick(R.id.send_button)
    public void onSendMessage(View src) {
        String msg = msgEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(msg)) {
            String to = tabHost.getCurrentTabTag();
            String msgTo = !MAIN_CHAT_TAG.equals(to) ? to : null;
            JSONObject jsonObject = mChatService.sendMessage(msg, msgTo);
            onAddMessage(jsonObject, to);
            msgEditText.setText(null);
        }
    }

    public void onAddMessage(@NonNull JSONObject object, @NonNull String to) {
        MessagesFragment fragment = (MessagesFragment) getSupportFragmentManager().findFragmentByTag(to);
        if (fragment == null) {
            onAddTab(to, to, object);
        } else {
            fragment.addMessage(object);
        }
    }
}
