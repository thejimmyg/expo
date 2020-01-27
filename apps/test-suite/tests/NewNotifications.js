'use strict';

import { Platform } from '@unimodules/core';
import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';

import * as TestUtils from '../TestUtils';
import { waitFor } from './helpers';

export const name = 'expo-notifications';

export async function test(t) {
  const shouldSkipTestsRequiringPermissions = await TestUtils.shouldSkipTestsRequiringPermissionsAsync();
  const describeWithPermissions = shouldSkipTestsRequiringPermissions ? t.xdescribe : t.describe;

  describeWithPermissions('expo-notifications', () => {
    t.describe('getDevicePushTokenAsync', () => {
      let subscription = null;
      let tokenFromEvent = null;
      let tokenFromMethodCall = null;

      t.beforeAll(() => {
        subscription = Notifications.addPushTokenListener(newEvent => {
          tokenFromEvent = newEvent;
        });
      });

      t.afterAll(() => {
        if (subscription) {
          subscription.remove();
          subscription = null;
        }
      });

      if (Platform.OS === 'android' || Platform.OS === 'ios') {
        t.it('resolves with a string', async () => {
          const devicePushToken = await Notifications.getDevicePushTokenAsync();
          t.expect(typeof devicePushToken.data).toBe('string');
          tokenFromMethodCall = devicePushToken;
        });
      }

      if (Platform.OS === 'web') {
        t.it('resolves with an object', async () => {
          const devicePushToken = await Notifications.getDevicePushTokenAsync();
          t.expect(typeof devicePushToken.data).toBe('object');
          tokenFromMethodCall = devicePushToken;
        });
      }

      t.it('emits an event with token (or not, if getDevicePushTokenAsync failed)', async () => {
        // It would be better to do `if (!tokenFromMethodCall) { pending(); } else { ... }`
        // but `t.pending()` still doesn't work.
        await waitFor(500);
        t.expect(tokenFromEvent).toEqual(tokenFromMethodCall);
      });
    });

    t.describe('getBadgeCountAsync', () => {
      t.it('resolves with an integer', async () => {
        const badgeCount = await Notifications.getBadgeCountAsync();
        t.expect(typeof badgeCount).toBe('number');
      });
    });

    const unsupportedDevices = [
      // If setBadgeCountAsync tests fail on your device,
      // add it to the list, so the test doesn't show up
      // as failed - we know ShortcutBadger doesn't work
      // on some devices and there's no reason to treat
      // it as a test fail.
      'Nokia 1 Plus',
      'Moto G Play',
    ];
    const describeOnSupportedDevices = unsupportedDevices.includes(Device.modelName)
      ? t.xdescribe
      : t.describe;
    describeOnSupportedDevices('setBadgeCountAsync', () => {
      t.it('sets a counter, retrievable with getBadgeCountAsync', async () => {
        try {
          const randomCounter = Math.ceil(Math.random() * 9) + 1;
          await Notifications.setBadgeCountAsync(randomCounter);
          const badgeCount = await Notifications.getBadgeCountAsync();
          t.expect(badgeCount).toBe(randomCounter);
        } catch (error) {
          console.info(`Model name of your device is '${Device.modelName}'.`);
          throw error;
        }
      });

      t.it('clears the counter', async () => {
        const clearingCounter = 0;
        await Notifications.setBadgeCountAsync(0);
        const badgeCount = await Notifications.getBadgeCountAsync();
        t.expect(badgeCount).toBe(clearingCounter);
      });
    });
  });
}
