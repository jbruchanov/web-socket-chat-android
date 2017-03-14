package com.scurab.android.websocketchat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.widget.TabHost;

import com.scurab.android.websocketchat.messaging.ChatService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Created by JBruchanov on 09/03/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    public static final String MAIN_CHAT_TAG = "#";

    @BeforeClass
    public static void setupClass() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(__ -> Schedulers.trampoline());
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @AfterClass
    public static void tearDownClass() {
        RxAndroidPlugins.reset();
    }

    @Test
    public void testHasMainChatTabOnStart() {
        final MainActivity mainActivity = Robolectric.buildActivity(MainActivity.class).create().resume().visible().get();
        final FragmentManager supportFragmentManager = mainActivity.getSupportFragmentManager();
        assertEquals(1, supportFragmentManager.getFragments().size());
        assertNotNull(supportFragmentManager.findFragmentByTag(MAIN_CHAT_TAG));
    }

    @Test
    public void testAddsNewTabWhenNewPrivateMessageIsComing() throws JSONException {
        final MainActivity mainActivity = Robolectric.buildActivity(MainActivity.class).create().resume().visible().get();
        final String testUser = "TestUser";
        final JSONObject sampleMessage = sampleMessage(testUser);
        mainActivity.onAddMessage(sampleMessage, testUser);
        mainActivity.tabHost.setCurrentTab(1);
        mainActivity.tabHost.setCurrentTabByTag(testUser);
        final MessagesFragment fragment = (MessagesFragment) mainActivity.getSupportFragmentManager().findFragmentByTag(testUser);
        assertEquals(1, fragment.getData().size());
        assertEquals(sampleMessage.toString(), fragment.getData().get(0).toString());
    }

    @Test
    public void testAddsNoTabWhenSendingPublicMessage() throws JSONException {
        final String msgText = "Sample";
        final HelpMainActivity mainActivity = Robolectric.buildActivity(HelpMainActivity.class).create().resume().visible().get();
        //MainOne added not over spy
        verify(mainActivity.tabHost, never()).addTab(any(TabHost.TabSpec.class), eq(MessagesFragment.class), any(Bundle.class));
        doReturn(sampleMessage("Test", msgText)).when(mainActivity.mChatService).sendMessage(any(), any());
        mainActivity.msgEditText.setText(msgText);
        mainActivity.onSendMessage(mainActivity.msgEditText);

        verify(mainActivity.tabHost, never()).addTab(any(TabHost.TabSpec.class), eq(MessagesFragment.class), any(Bundle.class));
        verify(mainActivity.mChatService).sendMessage(eq(msgText), isNull());
    }

    @Test
    public void testClearsEditTextAfterSendingMessage() throws JSONException {
        final String msgText = "Sample";
        final HelpMainActivity mainActivity = Robolectric.buildActivity(HelpMainActivity.class).create().resume().visible().get();
        doReturn(sampleMessage("Test", msgText)).when(mainActivity.mChatService).sendMessage(any(), any());
        mainActivity.msgEditText.setText(msgText);
        mainActivity.onSendMessage(mainActivity.msgEditText);
        assertThat(mainActivity.msgEditText).isEmpty();
    }

    @Test
    public void testLoadingMessagesOnStart() throws JSONException {
        final ActivityController<HelpMainActivity> controller = Robolectric.buildActivity(HelpMainActivity.class);
        HelpMainActivity mainActivity = controller.create().visible().get();
        mainActivity = spy(mainActivity);
        doNothing().when(mainActivity).onLoadMessages(any());
        final JSONArray dataArray = new JSONArray(Arrays.asList(sampleMessage("A"), sampleMessage("B")));
        doReturn(Observable.fromCallable(() ->
                new JSONObject()
                        .put("type", "msgs")
                        .put("data", dataArray)
        )).when(mainActivity.mChatService).start(any());

        verify(mainActivity, never()).onLoadMessages(any());
        mainActivity.startService("mobile");
        verify(mainActivity).onLoadMessages(eq(dataArray));
    }

    @Test
    public void testLoadingMessagesPassesDataToFragment() throws JSONException {
        HelpMainActivity mainActivity = new HelpMainActivity();
        final ActivityController<HelpMainActivity> controller = ActivityController.of(Robolectric.getShadowsAdapter(), mainActivity);
        controller.create().start().resume().visible().get();
        final JSONArray dataArray = new JSONArray(Arrays.asList(sampleMessage("A"), sampleMessage("B")));
        mainActivity.onLoadMessages(dataArray);
        MessagesFragment fragment = (MessagesFragment) mainActivity.getSupportFragmentManager().findFragmentByTag(MAIN_CHAT_TAG);
        assertEquals(2, fragment.getData().size());
    }

    @Test
    public void testSendingPublicMessage() throws JSONException {
        final String msgText = "Sample";
        final HelpMainActivity mainActivity = Robolectric.buildActivity(HelpMainActivity.class).create().resume().visible().get();
        mainActivity.msgEditText.setText(msgText);

        doReturn(sampleMessage("X")).when(mainActivity.mChatService).sendMessage(eq(msgText), isNull());
        mainActivity.onSendMessage(mainActivity.msgEditText);

        verify(mainActivity.mChatService).sendMessage(eq(msgText), isNull());
    }

    @Test
    public void testSendingPublicMessageAddsItemIntoList() throws JSONException {
        final String msgText = "Sample";
        final HelpMainActivity mainActivity = Robolectric.buildActivity(HelpMainActivity.class).create().resume().visible().get();
        MessagesFragment fragment = (MessagesFragment) mainActivity.getSupportFragmentManager().findFragmentByTag(MAIN_CHAT_TAG);
        mainActivity.msgEditText.setText(msgText);

        final JSONObject msg = sampleMessage("X");
        assertEquals(0, fragment.getData().size());
        assertThat(fragment.mRecyclerView).hasChildCount(0);

        doReturn(msg).when(mainActivity.mChatService).sendMessage(eq(msgText), isNull());
        mainActivity.onSendMessage(mainActivity.msgEditText);

        assertEquals(1, fragment.getData().size());

        assertThat(fragment.mRecyclerView).hasChildCount(1);
    }

    @Test
    public void testSendingPrivateMessage() throws JSONException {
        final String msgText = "Sample";
        final String to = "UserTest";
        final HelpMainActivity mainActivity = Robolectric.buildActivity(HelpMainActivity.class).create().resume().visible().get();
        mainActivity.msgEditText.setText(msgText);
        mainActivity.onAddTab(to, to);
        mainActivity.tabHost.setCurrentTabByTag(to);

        doReturn(sampleMessage("X")).when(mainActivity.mChatService).sendMessage(eq(msgText), any());
        mainActivity.onSendMessage(mainActivity.msgEditText);

        verify(mainActivity.mChatService).sendMessage(eq(msgText), eq(to));
    }

    @Test
    public void testSendingPrivateMessageAddsItemIntoList() throws JSONException {
        final String msgText = "Sample";
        final String to = "UserTest";
        final HelpMainActivity mainActivity = Robolectric.buildActivity(HelpMainActivity.class).create().resume().visible().get();
        mainActivity.msgEditText.setText(msgText);
        mainActivity.onAddTab(to, to);
        mainActivity.tabHost.setCurrentTabByTag(to);
        MessagesFragment fragment = (MessagesFragment) mainActivity.getSupportFragmentManager().findFragmentByTag(to);

        assertEquals(0, fragment.getData().size());
        assertThat(fragment.mRecyclerView).hasChildCount(0);

        doReturn(sampleMessage("X")).when(mainActivity.mChatService).sendMessage(eq(msgText), any());
        mainActivity.onSendMessage(mainActivity.msgEditText);

        assertEquals(1, fragment.getData().size());
        assertThat(fragment.mRecyclerView).hasChildCount(1);
    }

    private static JSONObject sampleMessage(String from) throws JSONException {
        return sampleMessage(from, "sample data");
    }

    private static JSONObject sampleMessage(String from, String data) throws JSONException {
        return new JSONObject()
                .put("from", from)
                .put("type", "msg")
                .put("received", new Date().toString())
                .put("data", data);
    }

    private static class HelpMainActivity extends MainActivity {
        ChatService mChatService;

        @NonNull
        @Override
        ChatService onCreateChatService() {
            mChatService = mock(ChatService.class);
            doReturn(Observable.fromCallable(
                    () -> new JSONObject().put("type", "none")
            )).when(mChatService).start(any());
            doReturn(Observable.fromCallable(() -> true))
                    .when(mChatService).onConnectionChanged();
            return mChatService;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            tabHost = spy(tabHost);
        }

        @Override
        public void onStart() {
            super.onStart();
        }
    }
}