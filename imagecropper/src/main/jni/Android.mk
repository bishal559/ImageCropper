LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := imagecropper
LOCAL_SRC_FILES := imageCrop.cpp

LOCAL_LDLIBS    := -landroid -llog -lz
LOCAL_STATIC_LIBRARIES := libpng libjpeg_static

include $(BUILD_SHARED_LIBRARY)

$(call import-module,libpng)
$(call import-module,libjpeg)