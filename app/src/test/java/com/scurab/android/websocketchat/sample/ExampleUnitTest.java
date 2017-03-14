package com.scurab.android.websocketchat.sample;

import android.app.Activity;
import android.widget.TextView;

import com.scurab.android.test.Waiter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static org.assertj.android.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testMockito() {
        Activity activity = mock(Activity.class);
        assertNotNull(activity);
    }

    @Test
    public void testRobolectric() {
        assertNotNull(RuntimeEnvironment.application);
        final TextView textView = new TextView(RuntimeEnvironment.application);
        assertThat(textView).isVisible();
    }

    @Test
    public void testMockitoWithFinalMethod() {
        FinalHelper helper = mock(FinalHelper.class);
        doReturn("Test").when(helper).sample();
        assertEquals("Test", helper.sample());
    }

    public static final class FinalHelper {
        public final String sample() {
            return "Sample";
        }
    }
}