# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn **
-keep public class de.julianostarek.materialfolderbrowser.** {
 public private protected *;
}
-keep public class org.jsoup.** {
  public private protected *;
}
-keep public class com.google.android.gms.cast.framework.** {
  public private protected *;
}
-keep public class android.support.v7.app.MediaRouteActionProvider {
  public private protected *;
}
-keep public class mobile.substance.** {
  public private protected *;
}
-keep public class co.metalab.** {
  public private protected *;
}
-keep public class kotlin.coroutines.** {
  public private protected *;
}