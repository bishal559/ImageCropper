# ImageCropper
The ImageCropper library for Android simplifies image cropping with a customizable interface, supporting drag-and-drop, zoom, and rotation. It integrates easily with Java and Kotlin, allowing users to crop images from the gallery or camera, and handles image orientation and memory efficiently. Perfect for seamless image cropping in Android apps.
# ImageCrop - Image Cropping Library for Android


[Screen_recording_20241021_104300.webm](https://github.com/user-attachments/assets/e35fe387-f6bc-4f00-9644-eece4d7faf11)


# Usage

1. Include the library as a local library project.

	```
	allprojects {
	   repositories {
	      jcenter()
	      maven { url "https://jitpack.io" }
	   }
	}
	```

    ``` implementation 'com.github.bishal:imagecropper:1.0.0' ``` - lightweight general solution

    ``` implementation 'com.github.bishal:imagecropper:1.0.0-native' ``` - get power of the native code to preserve image quality (+ about 1.5 MB to an apk size)

2. Add UCropActivity into your AndroidManifest.xml

    ```
    <activity
        android:name="com.bishal.imagecropper.ImageCropActivity"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar"/>
    ```

3. The uCrop configuration is created using the builder pattern.

   ```java
   ImageCrop.of(sourceUri, destinationUri)
       .withAspectRatio(16, 9)
       .withMaxResultSize(maxWidth, maxHeight)
       .start(context);
   ```

4. Override `onActivityResult` method and handle imageCrop result.

    ```java
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ImageCrop.REQUEST_CROP) {
            final Uri resultUri = ImageCrop.getOutput(data);
        } else if (resultCode == ImageCrop.RESULT_ERROR) {
            final Throwable cropError = ImageCrop.getError(data);
        }
    }
    ```

5. You may want to add this to your PROGUARD config:

    ```
    -dontwarn com.bishal.imagecrop**
    -keep class com.bishal.imagecrop** { *; }
    -keep interface com.bishal.imagecrop** { *; }
    ```

# Customization

If you want to let your users choose crop ratio dynamically, just do not call `withAspectRatio(x, y)`.

uCrop builder class has method `withOptions(UCrop.Options options)` which extends library configurations.

Currently, you can change:

   * image compression format (e.g. PNG, JPEG), compression
   * image compression quality [0 - 100]. PNG which is lossless, will ignore the quality setting.
   * whether all gestures are enabled simultaneously
   * maximum size for Bitmap that is decoded from source Uri and used within crop view. If you want to override the default behaviour.
   * toggle whether to show crop frame/guidelines
   * setup color/width/count of crop frame/rows/columns
   * choose whether you want rectangle or oval(`options.setCircleDimmedLayer(true)`) crop area
   * the UI colors (Toolbar, StatusBar, active widget state)
   * and more...

# Compatibility

  * Library - Android ICS 4.0+ (API 14) (Android GINGERBREAD 2.3+ (API 10) for versions <= 1.3.2)
  * Sample - Android ICS 4.0+ (API 14)
  * CPU - armeabi armeabi-v7a x86 x86_64 arm64-v8a (for versions >= 2.1.2)




### Version: 1.0.0

  * Create ImageCropper Library

### Let us know!

We’d be really happy if you sent us links to your projects where you use our component. Just send an email to bishaladhiakri668@gmail.com And do let us know if you have any questions or suggestion regarding the library.

#### Apps using ImageCrop

- [FoneMoney]([https://play.google.com/store/apps/details?id=com.twominds.thirty](https://play.google.com/store/apps/details?id=com.ksp.staremit)).

## License

    Copyright 2024, Bishal Adhikari

    Software doesn't collect, store or transfer data to Yalantis or third parties.
    Emplacement of this Software is carried out locally at device.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
