package expo.modules.splashscreen

import android.content.Context

import org.unimodules.core.ExportedModule
import org.unimodules.core.ModuleRegistry
import org.unimodules.core.Promise
import org.unimodules.core.interfaces.ExpoMethod

class SplashScreenModule(context: Context) : ExportedModule(context) {

  private var mModuleRegistry: ModuleRegistry? = null

  override fun getName(): String {
    return NAME
  }

  override fun onCreate(moduleRegistry: ModuleRegistry) {
    mModuleRegistry = moduleRegistry
  }

  @ExpoMethod
  fun preventAutoHideAsync(promise: Promise) {
    SplashScreen.preventAutoHide(
      { promise.resolve(null) },
      { m -> promise.reject(ERROR_TAG, m) }
    )
  }

  @ExpoMethod
  fun hideAsync(promise: Promise) {
    SplashScreen.hide(
      { promise.resolve(null) },
      { m -> promise.reject(ERROR_TAG, m) }
    )
  }

  companion object {
    private const val NAME = "ExpoSplashScreen"
    private const val ERROR_TAG = "ERR_SPLASH"
  }
}
