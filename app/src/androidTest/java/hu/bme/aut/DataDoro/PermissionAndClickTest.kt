package hu.bme.aut.t4xgko.DataDoro

import android.Manifest
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionAndClickTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var uiDevice: UiDevice

    @Before
    fun setUp() {
        uiDevice = UiDevice.getInstance(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testDenyPermissionsAndClick() {
        denyPermission(Manifest.permission.CAMERA)
        denyPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        onView(withId(R.id.recyclerDay))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withId(R.id.btnCamera)).perform(click())
        denyPermission(Manifest.permission.CAMERA)
    }

    private fun denyPermission(permission: String) {
        if (ApplicationProvider.getApplicationContext<Context>().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            uiDevice.findObject(UiSelector().text("DENY")).click()
        }
    }
}
