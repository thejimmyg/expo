package expo.modules.notifications.notifications.presentation.builders;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

import androidx.core.app.NotificationCompat;
import expo.modules.notifications.notifications.interfaces.NotificationBuilder;

/**
 * {@link NotificationBuilder} interpreting a couple of simple fields
 * from the "notification" JSON data field.
 */
public class ExpoNotificationBuilder implements NotificationBuilder {
  private static final String CONTENT_TITLE_KEY = "title";
  private static final String CONTENT_TEXT_KEY = "message";
  private static final String SOUND_KEY = "sound";
  private static final String BADGE_KEY = "badge";

  private static final String EXTRAS_BADGE_KEY = "badge";

  private static final long[] NO_VIBRATE_PATTERN = new long[]{0, 0};

  private final Context mContext;

  private JSONObject mNotificationRequest;

  public ExpoNotificationBuilder(Context context) {
    mContext = context;
  }

  @Override
  public ExpoNotificationBuilder setRemoteMessage(RemoteMessage remoteMessage) {
    Map<String, String> notificationRequestMap = remoteMessage.getData();
    mNotificationRequest = new JSONObject(notificationRequestMap);
    return this;
  }

  protected NotificationCompat.Builder createBuilder() {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, getChannelId());
    builder.setSmallIcon(mContext.getApplicationInfo().icon);

    // We're setting the content only if there is anything to set
    // otherwise the content title and text are displayed
    // as if they were empty strings.
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
      // Attach default notification sound to the NotificationCompat.Builder
      builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
      builder.setDefaults(NotificationCompat.DEFAULT_ALL); // sets default vibration too
    } else {
      // Remove any sound attached to the NotificationCompat.Builder
      builder.setSound(null);
      // Remove any sound attached by notification options.
      builder.setDefaults(0);
      // Remove any vibration pattern attached to the builder by overriding
      // it with a no-vibrate pattern. It also doubles as a cue for the OS
      // that given high priority it should be displayed as a heads-up notification.
      builder.setVibrate(NO_VIBRATE_PATTERN);
    }

    if (shouldSetBadge()) {
      // TODO: Set badge as an effect of presenting notification,
      //       not as an effect of building a notification.
      Bundle extras = builder.getExtras();
      extras.putInt(EXTRAS_BADGE_KEY, getBadgeCount());
      builder.setExtras(extras);
    }

    return builder;
  }

  @Override
  public Notification build() {
    return createBuilder().build();
  }

  /**
   * @return A {@link NotificationChannel} identifier to use for the notification.
   */
  protected String getChannelId() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // Returning null on incompatible platforms won't be an error.
      return null;
    }

    // We need a channel ID, but we don't know any. Let's use system-provided one as a fallback.
    Log.w("ExpoNotificationBuilder", "Using `NotificationChannel.DEFAULT_CHANNEL_ID` as channel ID for push notification. " +
        "Please provide a NotificationChannelsManager to provide builder with a fallback channel ID.");
    return NotificationChannel.DEFAULT_CHANNEL_ID;
  }

  private int getBadgeCount() {
    return mNotificationRequest.optInt(BADGE_KEY);
  }

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
