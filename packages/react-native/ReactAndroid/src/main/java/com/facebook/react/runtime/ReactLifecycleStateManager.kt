/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

 package com.facebook.react.runtime

 import android.app.Activity
 import androidx.annotation.UiThread
 import com.facebook.react.bridge.ReactContext
 import com.facebook.react.common.LifecycleState
 
 class ReactLifecycleStateManager(
     private val bridgelessReactStateTracker: BridgelessReactStateTracker
 ) {
     private var state: LifecycleState = LifecycleState.BEFORE_CREATE
 
     fun getLifecycleState(): LifecycleState {
         return state
     }
 
     @UiThread
     fun resumeReactContextIfHostResumed(currentContext: ReactContext, activity: Activity?) {
         if (state == LifecycleState.RESUMED) {
             bridgelessReactStateTracker.enterState("ReactContext.onHostResume()")
             currentContext.onHostResume(activity)
         }
     }
 
     @UiThread
     fun moveToOnHostResume(currentContext: ReactContext?, activity: Activity?) {
         if (state == LifecycleState.RESUMED) return
 
         currentContext?.let {
             bridgelessReactStateTracker.enterState("ReactContext.onHostResume()")
             it.onHostResume(activity)
         }
 
         state = LifecycleState.RESUMED
     }
 
     @UiThread
     fun moveToOnHostPause(currentContext: ReactContext?, activity: Activity?) {
         currentContext?.let {
             when (state) {
                 LifecycleState.BEFORE_CREATE -> {
                     // TODO: Investigate if we can remove this transition.
                     bridgelessReactStateTracker.enterState("ReactContext.onHostResume()")
                     it.onHostResume(activity)
                     bridgelessReactStateTracker.enterState("ReactContext.onHostPause()")
                     it.onHostPause()
                 }
                 LifecycleState.RESUMED -> {
                     bridgelessReactStateTracker.enterState("ReactContext.onHostPause()")
                     it.onHostPause()
                 }
                 else -> {
                     // No-op
                 }
             }
         }
 
         state = LifecycleState.BEFORE_RESUME
     }
 
     @UiThread
     fun moveToOnHostDestroy(currentContext: ReactContext?) {
         currentContext?.let {
             when (state) {
                 LifecycleState.BEFORE_RESUME -> {
                     bridgelessReactStateTracker.enterState("ReactContext.onHostDestroy()")
                     it.onHostDestroy()
                 }
                 LifecycleState.RESUMED -> {
                     bridgelessReactStateTracker.enterState("ReactContext.onHostPause()")
                     it.onHostPause()
                     bridgelessReactStateTracker.enterState("ReactContext.onHostDestroy()")
                     it.onHostDestroy()
                 }
                 else -> {
                     // No-op
                 }
             }
         }
 
         state = LifecycleState.BEFORE_CREATE
     }
 }
 