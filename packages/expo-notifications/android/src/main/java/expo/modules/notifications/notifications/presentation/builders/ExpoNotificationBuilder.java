package expo.modules.notifications.notifications.presentation.builders;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.NotificationCompat;
import expo.modules.notifications.notifications.interfaces.NotificationBuilder;
import me.leolin.shortcutbadger.ShortcutBadger;

public class ExpoNotificationBuilder implements NotificationBuilder {
  private static final String REMOTE_MESSAGE_DATA_NOTIFICATION_KEY = "notification";

  private static final String CONTENT_TITLE_KEY = "title";
  private static final String CONTENT_TEXT_KEY = "body";
  private static final String SOUND_KEY = "sound";
  private static final String BADGE_KEY = "badge";

  public static final String EXTRAS_BADGE_KEY = "badge";

  private static final long[] NO_VIBRATE_PATTERN = new long[]{0, 0};

  private final Context mContext;

  private JSONObject mNotificationRequest;
  private int mSmallIcon;

  public ExpoNotificationBuilder(Context context) {
    mContext = context;
    mSmallIcon = context.getApplicationInfo().icon;
  }

  @Override
  public ExpoNotificationBuilder setRemoteMessage(RemoteMessage remoteMessage) throws JSONException {
    String notificationRequestString = remoteMessage.getData().get(REMOTE_MESSAGE_DATA_NOTIFICATION_KEY);
    mNotificationRequest = new JSONObject(notificationRequestString);
    return this;
  }

  protected NotificationCompat.Builder createBuilder() {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, getChannelId());
    builder.setSmallIcon(mSmallIcon);

    if (mNotificationRequest.has(CONTENT_TITLE_KEY)) {
      builder.setContentTitle(mNotificationRequest.optString(CONTENT_TITLE_KEY));
    }
    if (mNotificationRequest.has(CONTENT_TEXT_KEY)) {
      builder.setContentText(mNotificationRequest.optString(CONTENT_TEXT_KEY));
    }

    if (shouldShowAlert()) {
      // Display as a heads-up notification
      builder.setPriority(NotificationCompat.PRIORITY_HIGH);
    } else {
      // Do not display as a heads-up notification, but show in the notification tray
      builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    if (shouldPlaySound()) {
      builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
      builder.setDefaults(NotificationCompat.DEFAULT_ALL); // sets default vibration too
    } else {
      builder.setSound(null);
      builder.setDefaults(0);
      builder.setVibrate(NO_VIBRATE_PATTERN);
    }

    // Maybe this code should be extracted to a separate
    // ExpoBadgeCompatibleNotificationBuilder.
    if (shouldSetBadge()) {
      // Forward information about badge count to set
      // to SetBadgeCountNotificationEffect.
      Bundle extras = builder.getExtras();
      extras.putInt(EXTRAS_BADGE_KEY, getBadgeCount());
      builder.setExtras(extras);
    }

    return builder;
  }

  @Override
  public Notification build() {
    Notification notification = createBuilder().build();

    // Maybe this code should be extracted to a separate
    // ExpoBadgeCompatibleNotificationBuilder.
    if (shouldSetBadge()) {
      // Xiaomi devices require this extra notification configuration step
      // https://github.com/leolin310148/ShortcutBadger/wiki/Xiaomi-Device-Support
      // Badge for other devices is set as an effect in SetBadgeCountNotificationEffect
      ShortcutBadger.applyNotification(mContext, notification, getBadgeCount());
    }

    return notification;
  }

  protected String getChannelId() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // Returning null on incompatible platforms won't be an error.
      return null;
    }

    // We need a channel ID, but we can't access the provider. Let's use system-provided one as a fallback.
    Log.w("ExpoNotificationBuilder", "Using `NotificationChannel.DEFAULT_CHANNEL_ID` as channel ID for push notification. " +
        "Please provide a NotificationChannelsManager to provide builder with a fallback channel ID.");
    return NotificationChannel.DEFAULT_CHANNEL_ID;
  }

  private int getBadgeCount() {
    return mNotificationRequest.optInt(BADGE_KEY);
  }

  // Behavior

  private boolean shouldShowAlert() {
    return mNotificationRequest.has(CONTENT_TITLE_KEY) || mNotificationRequest.has(CONTENT_TEXT_KEY);
  }

  private boolean shouldPlaySound() {
    return mNotificationRequest.optBoolean(SOUND_KEY);
  }

  private boolean shouldSetBadge() {
    return mNotificationRequest.has(BADGE_KEY);
  }
}
