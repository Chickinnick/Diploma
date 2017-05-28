package com.umbaba.bluetoothvswifidirect;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.umbaba.bluetoothvswifidirect.comparation.ComparationContract;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationFragment;
import com.umbaba.bluetoothvswifidirect.comparation.ComparationPresenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {


    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void startBluetoothActivity() {
        onView(withId(R.id.test_bluetooth_btn)).perform(click());
    }

}