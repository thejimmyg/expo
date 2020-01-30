package expo.modules.splashscreen

import android.app.Activity
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.facebook.react.ReactRootView
import java.lang.ref.WeakReference

object SplashScreen {
  private const val TAG = "SplashScreen"
  private const val VIEW_TEST_INTERVAL_MS = 20L

  private val handler = Handler()

  private var activity: WeakReference<Activity>? = null
  private var splashScreenViewContainer: ViewGroup? = null
  private var splashScreenView: View? = null
  private var autoHideEnabled = true

  /**
   * Show SplashScreen by mounting it next to the React Root View and completely covering it.
   * @param successCallback Callback to be called once SplashScreen is mounted in view hierarchy.
   * @param failureCallback Callback to be called once SplashScreen cannot be mounted.
   * @return Lambda function that would trigger finding valid viewHierarchy - Useful when ReactView root view isn't mounted from the start.
   */
  @JvmStatic
  @JvmOverloads
  fun show(
    activity: Activity,
    mode: SplashScreenMode,
    splashScreenConfigurator: SplashScreenConfigurator = ResourcesBasedSplashScreenConfigurator(),
    successCallback: () -> Unit = {},
    failureCallback: (reason: String) -> Unit = { Log.w(TAG, it) }
  ): () -> Unit {
    SplashScreen.activity = WeakReference(activity)
    activity.runOnUiThread {
      if (splashScreenView != null) {
        return@runOnUiThread failureCallback("Native SplashScreen is already mounted.")
      }
      splashScreenViewContainer = findContainerForSplashScreen(activity)
      if (splashScreenViewContainer == null) {
        return@runOnUiThread failureCallback("View hierarchy isn't ready to mount Native SplashScreen.")
      }
      splashScreenView = SplashScreenView(activity, mode, splashScreenConfigurator)
      splashScreenViewContainer!!.addView(splashScreenView).also { successCallback() }

      // launch autohide process
      if (autoHideEnabled) {
        searchForReactViewHierarchy({ hide({}, {}) })
      }
    }
    return { searchForReactViewHierarchy({ hide({}, {}) }) }
  }

  /**
   * Prevents SplashScreen from autohiding once React Views Hierarchy is mounted.
   * @param successCallback Callback to be called once SplashScreen could be successfully prevented from autohinding.
   * @param failureCallback Callback to be called upon failure in preventing SplashScreen from autohiding.
   */
  fun preventAutoHide(
    successCallback: () -> Unit,
    failureCallback: (reason: String) -> Unit
  ) {
    if (autoHideEnabled && splashScreenView != null) {
      autoHideEnabled = false
      successCallback()
    } else if (!autoHideEnabled) {
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
    val activity = activity?.get() ?: return failureCallback("Activity is no longer present.")
    activity.runOnUiThread {
      @Suppress("NAME_SHADOWING")
      val activity = SplashScreen.activity?.get()
        ?: return@runOnUiThread failureCallback("Activity is no longer present.")
      if (!activity.isFinishing && !activity.isDestroyed) {
        val splashScreenView = splashScreenView
          ?: return@runOnUiThread failureCallback("Native SplashScreen is already hidden.")
        splashScreenViewContainer?.removeView(splashScreenView).also { successCallback() }
          ?: return@runOnUiThread failureCallback("Native SplashScreen container is not available.")

        // restore initial state
        SplashScreen.splashScreenView = null
        autoHideEnabled = true
      } else {
        return@runOnUiThread failureCallback("Activity is not operable.")
      }
    }
  }

  /**
   * Waits for React Views Hierarchy to be mounted and once it happens fires callback, but only if autohiding is still enabled
   * @param hierarchyMountedCallback Callback to be called when React Views Hierarchy is detected.
   */
  private fun searchForReactViewHierarchy(hierarchyMountedCallback: () -> Unit, reactRootView: ViewGroup? = null) {
    val reactRoot: ViewGroup = reactRootView ?: activity?.get()?.let { findReactRootView(it) } ?: run {
      Log.w(TAG, "Couldn't find valid React Root View nor valid activity.")
      return
    }
    if (reactRoot.childCount > 0) {
      handler.postDelayed({
        // wait a little for possible `SplashScreen.preventAutoHide` from JS before autohiding
        if (autoHideEnabled) {
          hierarchyMountedCallback()
        }
      }, VIEW_TEST_INTERVAL_MS)
    } else {
      if (autoHideEnabled) {
        handler.postDelayed({ searchForReactViewHierarchy(hierarchyMountedCallback, reactRoot) }, VIEW_TEST_INTERVAL_MS)
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