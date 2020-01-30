// Copyright 2015-present 650 Industries. All rights reserved.

package host.exp.exponent;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Locale;

import host.exp.exponent.analytics.EXL;
import host.exp.exponent.utils.AsyncCondition;
import host.exp.exponent.utils.ColorParser;
import host.exp.expoview.R;

// TODO: remove this class, but preserve logic that finds image or cover it somehow
public class LoadingView extends RelativeLayout {

  public LoadingView(Context context) {
    super(context);
  }

  private String backgroundImageURL(final JSONObject manifest) {
    JSONObject splash;

    if (manifest.has("android")) {
      final JSONObject android = manifest.optJSONObject("android");
      if (android.has(ExponentManifest.MANIFEST_SPLASH_INFO_KEY)) {
        splash = android.optJSONObject(ExponentManifest.MANIFEST_SPLASH_INFO_KEY);

        // Use the largest available image in the `android.splash` object, or `splash.imageUrl` if none is available .
        final String[] keys = {"xxxhdpiUrl", "xxhdpiUrl", "xhdpiUrl", "hdpiUrl", "mdpiUrl", "ldpiUrl"};

        for (String key : keys) {
          if (splash.has(key) && splash.optString(key) != null) {
            return splash.optString(key);
          }
        }
      }
    }
    if (manifest.has(ExponentManifest.MANIFEST_SPLASH_INFO_KEY)) {
      splash = manifest.optJSONObject(ExponentManifest.MANIFEST_SPLASH_INFO_KEY);
      if (splash.has(ExponentManifest.MANIFEST_SPLASH_IMAGE_URL)) {
        return splash.optString(ExponentManifest.MANIFEST_SPLASH_IMAGE_URL);
      }
    }

    return null;
  }
}
