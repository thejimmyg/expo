package expo.modules.splashscreen

import android.content.Context
import android.widget.ImageView

/**
 * Default implementation bases on resources.
 * It should be used for main SplashScreen for the app.
 */
class ResourcesBasedSplashScreenConfigurator : SplashScreenConfigurator {

  override fun getBackgroundColor(context: Context): Int {
    return context.resources.getColor(R.color.splashscreen_background)
  }

  override fun configureImageView(context: Context, imageView: ImageView, mode: SplashScreenMode) {
    when (mode) {
      SplashScreenMode.NATIVE -> {
        imageView.setImageResource(R.drawable.splashscreen)
      }
      SplashScreenMode.COVER,
      SplashScreenMode.CONTAIN -> {
        imageView.setImageResource(R.drawable.splashscreen_image)
      }
    }
  }
}