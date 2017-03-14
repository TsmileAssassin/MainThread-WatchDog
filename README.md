# MainThread-WatchDog
A simple watchdog that found Android time-consuming operation in main thread.We can use it to find the reasons for the slow start of the application or the reasons for UI can not be drawn in a timely manner.
## How it works
Every 30 milliseconds, we will take out  the stack of the main thread.Then compared to the last stack, We can find out what method is stay on top of the stack of the main thread.we print it out and find the time-consuming method.
## Useage
 
```
compile 'com.tsmile.debug:mainthreadwatchdog:1.0.1'

// at the start point
MainThreadWatchDog.defaultInstance().startWatch();        

// at the end point
MainThreadWatchDog.defaultInstance().stopWatch();

// If it is a release version
MainThreadWatchDog.setDebug(false);
```

## Example
Here is the example log. you can run app project, see the log from logcat.
We can find TestApplication.initFunc1, TestApplication.initFunc2,
TestMainActivity.initUi, TestMainActivity.resumeUiStatus, TestApplication$1.run may have some problem.
```
 I/MainThreadWatchDog: ===============total:35 || >1078ms ===============
 I/MainThreadWatchDog: 26 || >802ms || 74.397% ||
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1405)
 I/MainThreadWatchDog: 16 || >495ms || 45.918% ||
                       com.tsmile.mainthreadwatchdog.TestApplication.onCreate(TestApplication.java:19)
                       com.android.tools.fd.runtime.BootstrapApplication.onCreate(BootstrapApplication.java:370)
                       android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1013)
                       android.app.ActivityThread.handleBindApplication(ActivityThread.java:4712)
                       android.app.ActivityThread.-wrap1(ActivityThread.java)
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1405)
 I/MainThreadWatchDog: 9 || >278ms || 25.788% ||
                       java.lang.Thread.sleep(Native Method)
                       java.lang.Thread.sleep(Thread.java:1031)
                       java.lang.Thread.sleep(Thread.java:985)
                       com.tsmile.mainthreadwatchdog.TestApplication.initFunc1(TestApplication.java:26)
                       com.tsmile.mainthreadwatchdog.TestApplication.onCreate(TestApplication.java:19)
                       com.android.tools.fd.runtime.BootstrapApplication.onCreate(BootstrapApplication.java:370)
                       android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1013)
                       android.app.ActivityThread.handleBindApplication(ActivityThread.java:4712)
                       android.app.ActivityThread.-wrap1(ActivityThread.java)
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1405)
 I/MainThreadWatchDog: 6 || >186ms || 17.254% ||
                       java.lang.Thread.sleep(Native Method)
                       java.lang.Thread.sleep(Thread.java:1031)
                       java.lang.Thread.sleep(Thread.java:985)
                       com.tsmile.mainthreadwatchdog.TestApplication.initFunc2(TestApplication.java:34)
                       com.tsmile.mainthreadwatchdog.TestApplication.onCreate(TestApplication.java:20)
                       com.android.tools.fd.runtime.BootstrapApplication.onCreate(BootstrapApplication.java:370)
                       android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1013)
                       android.app.ActivityThread.handleBindApplication(ActivityThread.java:4712)
                       android.app.ActivityThread.-wrap1(ActivityThread.java)
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1405)
 I/MainThreadWatchDog: 8 || >246ms || 22.82% ||
                       android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2476)
                       android.app.ActivityThread.-wrap11(ActivityThread.java)
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1344)
 I/MainThreadWatchDog: 4 || >123ms || 11.41% ||
                       android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2369)
                       android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2476)
                       android.app.ActivityThread.-wrap11(ActivityThread.java)
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1344)
 I/MainThreadWatchDog: 3 || >93ms || 8.627% ||
                       java.lang.Thread.sleep(Native Method)
                       java.lang.Thread.sleep(Thread.java:1031)
                       java.lang.Thread.sleep(Thread.java:985)
                       com.tsmile.mainthreadwatchdog.TestMainActivity.initUi(TestMainActivity.java:37)
                       com.tsmile.mainthreadwatchdog.TestMainActivity.onCreate(TestMainActivity.java:19)
                       android.app.Activity.performCreate(Activity.java:6251)
                       android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1107)
                       android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2369)
                       android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2476)
                       android.app.ActivityThread.-wrap11(ActivityThread.java)
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1344)
 I/MainThreadWatchDog: 3 || >92ms || 8.534% ||
                       android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3134)
                       android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2481)
                       android.app.ActivityThread.-wrap11(ActivityThread.java)
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1344)
 I/MainThreadWatchDog: 2 || >61ms || 5.659% ||
                       java.lang.Thread.sleep(Native Method)
                       java.lang.Thread.sleep(Thread.java:1031)
                       java.lang.Thread.sleep(Thread.java:985)
                       com.tsmile.mainthreadwatchdog.TestMainActivity.resumeUiStatus(TestMainActivity.java:45)
                       com.tsmile.mainthreadwatchdog.TestMainActivity.onResume(TestMainActivity.java:32)
                       android.app.Instrumentation.callActivityOnResume(Instrumentation.java:1258)
                       android.app.Activity.performResume(Activity.java:6327)
                       android.app.ActivityThread.performResumeActivity(ActivityThread.java:3092)
                       android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3134)
                       android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2481)
                       android.app.ActivityThread.-wrap11(ActivityThread.java)
                       android.app.ActivityThread$H.handleMessage(ActivityThread.java:1344)
 I/MainThreadWatchDog: 7 || >216ms || 20.037% ||
                       android.os.Handler.handleCallback(Handler.java:739)
 I/MainThreadWatchDog: 3 || >93ms || 8.627% ||
                       java.lang.Thread.sleep(Native Method)
                       java.lang.Thread.sleep(Thread.java:1031)
                       java.lang.Thread.sleep(Thread.java:985)
                       com.tsmile.mainthreadwatchdog.TestApplication$1.run(TestApplication.java:45)
                       android.os.Handler.handleCallback(Handler.java:739)
 I/MainThreadWatchDog: 3 || >92ms || 8.534% ||
                       android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:1730)
                       android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:1115)
                       android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:6023)
                       android.view.Choreographer$CallbackRecord.run(Choreographer.java:858)
                       android.view.Choreographer.doCallbacks(Choreographer.java:670)
                       android.view.Choreographer.doFrame(Choreographer.java:606)
                       android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:844)
                       android.os.Handler.handleCallback(Handler.java:739)

```

