package expo.modules.notifications.notifications.interfaces;

import android.app.Notification;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;

/**
 * An object capable of building a {@link Notification} based
 * on {@link RemoteMessage}.
 */
public interface NotificationBuilder {
  /**
   * Pass in {@link RemoteMessage} based on which the notification should be based.
   *
   * @param remoteMessage {@link RemoteMessage} on which the notification should be based.
   * @return The same instance of {@link NotificationBuilder} updated with the remote message.
   * @throws JSONException Thrown if data contained in {@link RemoteMessage} could not have been
   *                       interpreted by the builder.
   */
  NotificationBuilder setRemoteMessage(RemoteMessage remoteMessage) throws JSONException;

  /**
   * Builds the notification based on passed in data.
   *
   * @return Built notification.
   */
  Notification build();
}
