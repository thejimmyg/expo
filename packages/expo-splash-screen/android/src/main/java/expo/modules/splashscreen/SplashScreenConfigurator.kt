package expo.modules.splashscreen

import android.content.Context
import android.widget.ImageView

/**
 * This interface is responsible for providing flow-dependent resources for proper SplashScreen initialization.
 */
interface SplashScreenConfigurator {
  /**
   * Provide color that would be set as background of splash screen.
   */
  fun getBackgroundColor(context: Context): Int
  /**
   * Purpose of this method is to provide source for imageView. Additionally you can modify imageView behaviour.
   */
  fun configureImageView(context: Context, imageView: ImageView, mode: SplashScreenMode)
}
