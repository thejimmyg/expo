package host.exp.exponent.experience.SplashScreen;

import android.app.Activity;
import android.util.Log;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;
import expo.modules.splashscreen.SplashScreen;
import kotlin.jvm.functions.Function0;

public class ExperienceSplashScreen {

  static private final String TAG = ExperienceSplashScreen.class.getSimpleName();

  private WeakReference<Activity> mActivity;
  private @Nullable ExperienceLoadingView mLoadingView;
  private @Nullable ExperienceSplashScreenConfig mSplashScreenConfig;

  /**
   * Showing splash screen using unimodule automatically invokes checking for autohiding, but it fails when React Root View is not mounted yet.
   * Therefore we need some way to trigger autohiding at later point in time.
   */
  private @Nullable Function0 splashScreenAutoHidingCallback = null;

  public ExperienceSplashScreen(Activity activity) {
    mActivity = new WeakReference<>(activity);
  }

  /**
   * Shows SplashScreen and mounts LoadingView
   */
  public void showSplashScreen(@Nullable JSONObject manifest) {
    if (mSplashScreenConfig == null) {
      mSplashScreenConfig = ExperienceSplashScreenConfig.parseManifest(manifest);
    }

    boolean showDefaultIcon = manifest == null;

    ExperienceSplashScreenConfigurator splashScreenConfigurator = new ExperienceSplashScreenConfigurator(mSplashScreenConfig, showDefaultIcon);

    splashScreenAutoHidingCallback = SplashScreen.show(
      mActivity.get(),
      mSplashScreenConfig.getMode(),
      splashScreenConfigurator
    );

    if (manifest != null) {
      // TODO: handle this
      mLoadingView = new ExperienceLoadingView(mActivity.get());
      mLoadingView.show(mActivity.get());
    }
  }

  public void updateLoadingProgress(String status, Integer done, Integer total) {
    if (mLoadingView == null) {
      Log.w(TAG, "LoadingView is null. Cannot update progress bar.");
      return;
    }
    mLoadingView.updateProgress(status, done, total);
  }

  /**
   * Marks loading as completed and hide LoadingView.
   * Possibly
   */
  public void finishLoading(FinishLoadingCallback callback) {
    if (splashScreenAutoHidingCallback != null) {
      splashScreenAutoHidingCallback.invoke();
      splashScreenAutoHidingCallback = null;
    }

    if (mLoadingView == null) {
      Log.w(TAG, "LoadingView is null. Cannot hide it.");
      return;
    }

    // TODO: ExperienceActivityUtils.setRootViewBackgroundColor(mManifest, getRootView());
    mLoadingView.hide();
    mLoadingView = null;
    callback.invoke();
  }

  public void interruptLoading(Exception error) {

  }

  @FunctionalInterface
  public interface FinishLoadingCallback {
    void invoke();
  }
}
