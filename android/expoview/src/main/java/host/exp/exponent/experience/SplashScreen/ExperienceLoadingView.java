package host.exp.exponent.experience.SplashScreen;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import host.exp.expoview.Exponent;
import host.exp.expoview.R;

class ExperienceLoadingView extends RelativeLayout {

  private static final long PROGRESS_BAR_DELAY_MS = 2500;

  private PopupWindow mLoadingPopup;
  private View mLoadingView;
  private ProgressBar mProgressBar;
  private View mStatusBarView;
  private TextView mStatusTextView;
  private TextView mPercentageTextView;

  private Handler mProgressBarHandler = new Handler();

  ExperienceLoadingView(Context context) {
    super(context);
    LayoutInflater inflater = LayoutInflater.from(context);
    mLoadingView = inflater.inflate(R.layout.loading_view, null);
    mProgressBar = findViewById(R.id.progress_bar);
    mStatusBarView = findViewById(R.id.status_bar);
    mStatusTextView = findViewById(R.id.status_text_view);
    mPercentageTextView = findViewById(R.id.percentage_text_view);

    mLoadingPopup = new PopupWindow(mLoadingView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    mLoadingPopup.setTouchable(false);
  }

  public void show(Activity activity) {
    if (mLoadingPopup.isShowing()) {
      // already showing
      return;
    }

    Exponent.getInstance().runOnUiThread(() -> {
      mLoadingPopup.showAtLocation(activity.getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 80);
      showProgressBar();
    });
  }

  private void showProgressBar() {
    mProgressBarHandler.postDelayed(() -> {
      mProgressBar.setVisibility(View.VISIBLE);
      AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
      animation.setDuration(250);
      animation.setInterpolator(new AccelerateDecelerateInterpolator());
      animation.setFillAfter(true);
      mProgressBar.startAnimation(animation);
    }, PROGRESS_BAR_DELAY_MS);
  }

  public void hide() {
    if (!mLoadingPopup.isShowing()) {
      return;
    }
    Exponent.getInstance().runOnUiThread(() -> {
      hideProgressBar();
      mLoadingPopup.dismiss();
    });
  }

  private void hideProgressBar() {
    mProgressBarHandler.removeCallbacksAndMessages(null);
    mProgressBar.clearAnimation();

    if (mProgressBar.getVisibility() == View.VISIBLE) {
      AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
      animation.setDuration(250);
      animation.setInterpolator(new AccelerateDecelerateInterpolator());
      animation.setFillAfter(true);
      animation.setAnimationListener(new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
          mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
      });
      mProgressBar.startAnimation(animation);
    }
  }

  public void updateProgress(String status, Integer done, Integer total) {
    Exponent.getInstance().runOnUiThread(() -> {
      mStatusBarView.setVisibility(VISIBLE);
      mStatusTextView.setText(status != null ? status : "Building JavaScript bundle...");
      if (done != null && total != null && total > 0) {
        float percent = ((float)done / (float)total * 100.f);
        mPercentageTextView.setText(String.format(Locale.getDefault(), "%.2f%%", percent));
      }
    });
  }
}
