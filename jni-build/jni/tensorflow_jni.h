/* 
   
Copyright 2016 Narrative Nithts Inc. All Rights Reserved.
Copyright 2015 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

#ifndef ORG_TENSORFLOW_JNI_TENSORFLOW_JNI_H_  // NOLINT
#define ORG_TENSORFLOW_JNI_TENSORFLOW_JNI_H_  // NOLINT

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif  // __cplusplus

#define TENSORFLOW_METHOD(METHOD_NAME) \
  Java_edu_berkeley_datascience_contextualhealer_activity_ActivityDetector_##METHOD_NAME  // NOLINT

JNIEXPORT jint JNICALL
TENSORFLOW_METHOD(init)(
    JNIEnv* env, jobject thiz, jobject java_asset_manager,
    jstring model);


JNIEXPORT jint JNICALL
TENSORFLOW_METHOD(getActivityClass)(
    JNIEnv* env, jobject thiz, jint featurescount, jdoubleArray input);

JNIEXPORT jdouble JNICALL
TENSORFLOW_METHOD(getTestPrediction)(
    JNIEnv* env, jobject thiz, jdoubleArray input);


#ifdef __cplusplus
}  // extern "C"
#endif  // __cplusplus

#endif  // ORG_TENSORFLOW_JNI_TENSORFLOW_JNI_H_  // NOLINT
