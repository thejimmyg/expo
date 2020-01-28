package main.kotlin.expo.modules.splashscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import expo.modules.splashscreen.R
import java.lang.annotation.Native

@SuppressLint("ViewConstructor")
/**
 * @param container - ViewGroup that holds ReactNative view hierarchy and would be used add this view
 */
class SplashScreenView(activity: Activity, private val container: ViewGroup, mode: SplashScreenMode) : RelativeLayout(activity) {
  private val imageView = ImageView(activity)

  init {
    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    setBackgroundColor(activity.resources.getColor(R.color.splashscreen_background))
    addView(imageView)

    imageView.layoutParams = LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT).also {
      it.addRule(CENTER_IN_PARENT, TRUE)
    }
    imageView.scaleType = mode.scaleType
    when (mode) {
      SplashScreenMode.NATIVE -> {
        imageView.setImageResource(R.drawable.splashscreen)
      }
      SplashScreenMode.CONTAIN -> {
        imageView.setImageResource(R.drawable.splashscreen_image)
        imageView.adjustViewBounds = true
      }
      SplashScreenMode.COVER -> {
        imageView.setImageResource(R.drawable.splashscreen_image)
      }
    }
  }

  fun show() {
    container.addView(this)
  }

  fun hide() {
    container.removeView(this)
  }
}