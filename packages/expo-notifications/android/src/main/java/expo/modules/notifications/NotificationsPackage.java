package expo.modules.notifications;

import android.content.Context;

import org.unimodules.core.BasePackage;
import org.unimodules.core.ExportedModule;
import org.unimodules.core.interfaces.InternalModule;
import org.unimodules.core.interfaces.SingletonModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import expo.modules.notifications.installationid.InstallationIdProvider;
import expo.modules.notifications.notifications.presentation.ExpoNotificationBuilderFactory;
import expo.modules.notifications.tokens.PushTokenManager;
import expo.modules.notifications.tokens.PushTokenModule;

public class NotificationsPackage extends BasePackage {
  @Override
  public List<InternalModule> createInternalModules(Context context) {
    return Collections.singletonList((InternalModule) new ExpoNotificationBuilderFactory());
  }

  @Override
  public List<ExportedModule> createExportedModules(Context context) {
    return Arrays.asList(
        new PushTokenModule(context),
        new InstallationIdProvider(context)
    );
  }

  @Override
  public List<SingletonModule> createSingletonModules(Context context) {
    return Collections.singletonList((SingletonModule) new PushTokenManager());
  }
}
