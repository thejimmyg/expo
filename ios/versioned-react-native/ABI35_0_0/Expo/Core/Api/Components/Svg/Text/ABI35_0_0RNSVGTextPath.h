/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <Foundation/Foundation.h>
#import <CoreText/CoreText.h>
#import "ABI35_0_0RNSVGText.h"
#import "ABI35_0_0RNSVGLength.h"

@interface ABI35_0_0RNSVGTextPath : ABI35_0_0RNSVGText

@property (nonatomic, strong) NSString *href;
@property (nonatomic, strong) NSString *side;
@property (nonatomic, strong) NSString *method;
@property (nonatomic, strong) NSString *midLine;
@property (nonatomic, strong) NSString *spacing;
@property (nonatomic, strong) ABI35_0_0RNSVGLength *startOffset;

- (void)getPathLength:(CGFloat*)length lineCount:(NSUInteger*)lineCount lengths:(NSArray* __strong *)lengths lines:(NSArray* __strong *)lines isClosed:(BOOL*)isClosed;


@end
