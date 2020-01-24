package main.kotlin.expo.modules.splashscreen

import android.widget.ImageView

enum class SplashScreenMode(val scaleType: ImageView.ScaleType) {
  CONTAIN(ImageView.ScaleType.FIT_CENTER),
  COVER(ImageView.ScaleType.CENTER_CROP),
  NATIVE(ImageView.ScaleType.CENTER);
}