// Copyright 2018-present 650 Industries. All rights reserved.

#import <EXNotifications/EXBadgeModule.h>
#import <UMCore/UMUtilities.h>

@implementation EXBadgeModule

UM_EXPORT_MODULE(ExpoBadgeModule)

// Accessing applicationIconBadge requires main queue.
- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

UM_EXPORT_METHOD_AS(getBadgeCountAsync,
                    getBadgeCountAsync:(UMPromiseResolveBlock)resolve reject:(UMPromiseRejectBlock)reject)
{
  resolve(@([UMSharedApplication() applicationIconBadgeNumber]));
}

UM_EXPORT_METHOD_AS(setBadgeCountAsync,
                    setBadgeCountAsync:(NSNumber *)badgeCount
                    resolve:(UMPromiseResolveBlock)resolve
                    reject:(UMPromiseRejectBlock)reject)
{
  [UMSharedApplication() setApplicationIconBadgeNumber:badgeCount.integerValue];
  resolve(nil);
}

@end
