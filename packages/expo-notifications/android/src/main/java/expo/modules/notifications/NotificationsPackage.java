package expo.modules.notifications;

import android.content.Context;

import org.unimodules.core.BasePackage;
import org.unimodules.core.ExportedModule;
import org.unimodules.core.interfaces.InternalModule;
import org.unimodules.core.interfaces.SingletonModule;

import java.util.Arrays;
import java.util.List;

import expo.modules.notifications.badge.BadgeModule;
import expo.modules.notifications.badge.ExpoBadgeManager;
import expo.modules.notifications.installationid.InstallationIdProvider;
import expo.modules.notifications.notifications.presentation.ExpoNotificationBuilderFactory;
import expo.modules.notifications.notifications.presentation.ExpoNotificationPresentationEffectsManager;
import expo.modules.notifications.badge.SetBadgeCountNotificationEffect;
import expo.modules.notifications.tokens.PushTokenManager;
import expo.modules.notifications.tokens.PushTokenModule;

public class NotificationsPackage extends BasePackage {
  @Override
  public List<InternalModule> createInternalModules(Context context) {
    return Arrays.asList(
        new ExpoNotificationBuilderFactory(),
        new ExpoNotificationPresentationEffectsManager(),
        new SetBadgeCountNotificationEffect(context)
    );
  }

  @Override
  public List<ExportedModule> createExportedModules(Context context) {
    return Arrays.asList(
        new BadgeModule(context),
        new PushTokenModule(context),
        new InstallationIdProvider(context)
    );
  }

  @Override
  public List<SingletonModule> createSingletonModules(Context context) {
    return Arrays.asList(
        new PushTokenManager(),
        new ExpoBadgeManager(context)
    );
  }
}
