package main.kotlin.expo.modules.splashscreen

import android.app.Activity
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.facebook.react.ReactRootView
import java.lang.ref.WeakReference

object SplashScreen {
  private const val LOG_TAG = "SPLASH"
  private const val VIEW_TEST_INTERVAL_MS = 20L

  private val handler = Handler()

  private var activity: WeakReference<Activity>? = null
  private var splashScreenView: SplashScreenView? = null
  private var autohideEnabled = true

  /**
   * Show SplashScreen by mounting it next to the React Root View and completely covering it.
   * @param successCallback Callback to be called once SplashScreen is mounted in view hierarchy.
   * @param failureCallback Callback to be called once SplashScreen cannot be mounted.
   */
  @JvmStatic
  @JvmOverloads
  fun show(
    activity: Activity,
    mode: SplashScreenMode,
    successCallback: () -> Unit = {},
    failureCallback: (reason: String) -> Unit = { Log.w(LOG_TAG, it) }
  ) {
    this.activity = WeakReference(activity)
    activity.runOnUiThread {
      if (splashScreenView != null) {
        return@runOnUiThread failureCallback("Native SplashScreen is already mounted.")
      }
      splashScreenView = findContainerForSplashScreen(activity)?.let { SplashScreenView(activity, it, mode) }
        ?: return@runOnUiThread failureCallback("View hierarchy isn't ready to mount Native SplashScreen.")
      splashScreenView!!.show().also { successCallback() }

      // launch autohide process
      checkReactViewHierarchy { this.hide({}, {}) }
    }
  }

  /**
   * Prevents SplashScreen from autohiding once React Views Hierarchy is mounted.
   * @param successCallback Callback to be called once SplashScreen could be successfully prevented from autohinding.
   * @param failureCallback Callback to be called upon failure in preventing SplashScreen from autohiding.
   */
  fun preventAutohide(
    successCallback: () -> Unit,
    failureCallback: (reason: String) -> Unit
  ) {
    if (this.autohideEnabled && this.splashScreenView != null) {
      this.autohideEnabled = false
      successCallback()
    } else if (!this.autohideEnabled) {
      failureCallback("Native SplashScreen autohiding is already prevented.")
    } else {
      failureCallback("Native SplashScreen is already hidden.")
    }
  }

  /**
   * Hides SplashScreen.
   * @param successCallback Callback to be called once SplashScreen is removed from view hierarchy.
   * @param failureCallback Callback to be called upon failure in hiding SplashScreen.
   */
  fun hide(
    successCallback: () -> Unit,
    failureCallback: (reason: String) -> Unit
  ) {
    val activity = this.activity?.get() ?: return failureCallback("Activity is no longer present.")
    activity.runOnUiThread {
      @Suppress("NAME_SHADOWING")
      val activity = this.activity?.get()
        ?: return@runOnUiThread failureCallback("Activity is no longer present.")
      if (!activity.isFinishing && !activity.isDestroyed) {
        val splashScreenView = this.splashScreenView
          ?: return@runOnUiThread failureCallback("Native SplashScreen is already hidden.")
        splashScreenView.hide().also { successCallback() }

        // restore initial state
        this.splashScreenView = null
        this.autohideEnabled = true
      } else {
        return@runOnUiThread failureCallback("Activity is not operable.")
      }
    }
  }

  /**
   * Waits for React Views Hierarchy to be mounted and once it happens fires callback, but only if autohiding is still enabled
   * @param hierarchyMountedCallback Callback to be called when React Views Hierarchy is detected.
   */
  private fun checkReactViewHierarchy(hierarchyMountedCallback: () -> Unit) {
    val reactRoot: ViewGroup = activity?.get()?.let { findReactRootView(it) } ?: run {
      Log.w(LOG_TAG, "Couldn't find valid view hierarchy nor valid activity.")
      return
    }
    if (reactRoot.childCount > 0) {
      handler.postDelayed({
        // wait a little for possible `SplashScreen.preventAutoHide` from JS before autohiding
        if (autohideEnabled) {
          hierarchyMountedCallback()
        }
      }, VIEW_TEST_INTERVAL_MS)
    } else {
      if (autohideEnabled) {
        handler.postDelayed({ checkReactViewHierarchy(hierarchyMountedCallback) }, VIEW_TEST_INTERVAL_MS)
      }
    }
  }

  private fun findReactRootView(activity: Activity): ViewGroup? {
    return activity.findViewById<ViewGroup>(android.R.id.content)?.let { findReactRootView(it) }
  }

  private fun findReactRootView(view: View): ViewGroup? {
    if (view is ReactRootView) {
      return view
    }
    if (view is ViewGroup) {
      for (idx in 0 until view.childCount) {
        findReactRootView(view.getChildAt(idx))?.let { return@findReactRootView it }
      }
    }
    return null
  }

  private fun findContainerForSplashScreen(activity: Activity): ViewGroup? {
    return activity.findViewById(android.R.id.content)
  }
}