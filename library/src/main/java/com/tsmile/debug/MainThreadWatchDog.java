package com.tsmile.debug;

import android.os.Looper;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * From time to time to take the main thread stack,
 * to speculate the implementation of time-consuming method
 * <p>
 * Created by tsmile on 16/2/26.
 */
public class MainThreadWatchDog extends Thread {

    private static final String TAG = "MainThreadWatchDog";
    private static boolean sDebug = true;
    private static MainThreadWatchDog sMainThreadWatchDog = new MainThreadWatchDog();
    private HashMap<WrappedStackTraceElement, TimeCounter> mLegacyStackTrace = new LinkedHashMap<>();
    private final Map<WrappedStackTraceElement, TimeCounter> mAllCareStackTrace = new LinkedHashMap<>();

    private long mSleepInterval = 30;
    private TimeCounter mTotalTime = new TimeCounter();
    private volatile boolean mStarted;

    private long mLastTimeDumpTrace;

    public static MainThreadWatchDog defaultInstance() {
        return sMainThreadWatchDog;
    }

    public static void setDebug(boolean debug) {
        sDebug = debug;
    }

    public synchronized void startWatch() {
        if (!sDebug || mStarted) {
            return;
        }
        mTotalTime.reset();
        mStarted = true;
        mLastTimeDumpTrace = 0;
        setPriority(Thread.NORM_PRIORITY);
        start();
    }

    public synchronized void stopWatch() {
        if (!sDebug || !mStarted) {
            return;
        }
        mStarted = false;


        List<PriorityStackTraceProfile> priorityStackTraceProfileList
                = new ArrayList<>();
        synchronized (mAllCareStackTrace) {
            for (WrappedStackTraceElement wrappedStackTraceElement : mAllCareStackTrace.keySet()) {
                TimeCounter timeCounter = mAllCareStackTrace.get(wrappedStackTraceElement);
                PriorityStackTraceProfile priorityStackTraceProfile = new PriorityStackTraceProfile(wrappedStackTraceElement,
                        timeCounter, 1d * timeCounter.getTotalTime() / mTotalTime.getTotalTime());
                priorityStackTraceProfileList.add(priorityStackTraceProfile);
            }
        }

        PriorityStackTraceProfile lastProfile = null;
        for (int i = priorityStackTraceProfileList.size() - 1; i >= 0; i--) {
            PriorityStackTraceProfile profile = priorityStackTraceProfileList.get(i);
            if (lastProfile != null && lastProfile.contains(profile)) {
                profile.isDuplicate = true;
            } else {
                profile.isDuplicate = false;
            }
            lastProfile = profile;
        }

        String totalString = "===============" +
                "total:" + mTotalTime.getTotalCount() + " ||" + " >" +
                mTotalTime.getTotalTime() + "ms ===============\n";
        Log.i(TAG, totalString);
        for (PriorityStackTraceProfile profile : priorityStackTraceProfileList) {
            if (!profile.isDuplicate) {
                String everyTraceString = String.valueOf(profile.timeCounter.getTotalCount()) + " ||" +
                        " >" + profile.timeCounter.getTotalTime() + "ms || " +
                        profile.incPercent + " ||" + "\n" +
                        profile.stackString;
                Log.i(TAG, everyTraceString);
            }
        }
    }

    @Override
    public void run() {
        setName(TAG);
        while (!isInterrupted() && mStarted) {
            long begin = System.nanoTime();
            // 1 dump main thread
            final Thread mainThread = Looper.getMainLooper().getThread();
            final StackTraceElement[] mainStackTrace = mainThread.getStackTrace();
            long thisTimeDumpTrace = System.nanoTime() / 1000000;
            long realSleepTime;
            if (mLastTimeDumpTrace == 0) {
                realSleepTime = mSleepInterval;
            } else {
                realSleepTime = thisTimeDumpTrace - mLastTimeDumpTrace;
            }
            mLastTimeDumpTrace = thisTimeDumpTrace;
            if (mainStackTrace != null) {
                int startIndex = 0;
                int endIndex = mainStackTrace.length - 1;
                for (int i = mainStackTrace.length - 1; i >= 0; i--) {
                    StackTraceElement stackTraceElement = mainStackTrace[i];
                    if (stackTraceElement.getMethodName().equals("dispatchMessage")
                            && stackTraceElement.getClassName().equals("android.os.Handler")) {
                        endIndex = i - 1;
                        break;
                    }
                }

                HashMap<WrappedStackTraceElement, TimeCounter> newLegacyStackTrace = new HashMap<>();
                if (endIndex != -1) {
                    boolean maybeMatch = true;
                    StringBuilder stackStringBuilder = new StringBuilder();
                    for (int j = endIndex; j >= startIndex; j--) {
                        StackTraceElement stackTraceElement = mainStackTrace[j];
                        WrappedStackTraceElement wrappedStackTraceElement = new WrappedStackTraceElement(stackTraceElement,
                                stackStringBuilder.toString());
                        stackStringBuilder.insert(0, stackTraceElement.toString() + "\n");
                        TimeCounter countThisTime = mLegacyStackTrace.get(wrappedStackTraceElement);
                        if (countThisTime != null && maybeMatch) {
                            if (newLegacyStackTrace.get(wrappedStackTraceElement) == null) {
                                countThisTime.addTime(realSleepTime);
                                newLegacyStackTrace.put(wrappedStackTraceElement, countThisTime);

                                synchronized (mAllCareStackTrace) {
                                    TimeCounter countTotal = mAllCareStackTrace.get(wrappedStackTraceElement);
                                    if (countTotal == null) {
                                        countTotal = new TimeCounter();
                                    }
                                    countTotal.addTime(realSleepTime);
                                    mAllCareStackTrace.put(wrappedStackTraceElement, countTotal);
                                }
                            }
                        } else {
                            maybeMatch = false;
                            newLegacyStackTrace.put(wrappedStackTraceElement, new TimeCounter());
                        }
                    }
                }
                mLegacyStackTrace = newLegacyStackTrace;
                mTotalTime.addTime(realSleepTime);
            }

            long end = System.nanoTime();
            long durationInMs = (end - begin) / 1000000;
            // 2 sleep
            if (mSleepInterval > durationInMs && durationInMs >= 0) {
                try {
                    Thread.sleep(mSleepInterval - durationInMs);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private static class PriorityStackTraceProfile implements
            Comparable<PriorityStackTraceProfile> {
        private static NumberFormat percent = NumberFormat.getPercentInstance();
        public String stackString;
        public TimeCounter timeCounter;
        public String incPercent;
        public boolean isDuplicate;

        PriorityStackTraceProfile(WrappedStackTraceElement wrappedStackTraceElement,
                                  TimeCounter timeCounter, double incPercent) {
            percent = new DecimalFormat("0.00#%");
            this.timeCounter = timeCounter;
            this.stackString = wrappedStackTraceElement.stackTraceElement.toString() + "\n" +
                    wrappedStackTraceElement.callStackString;
            this.incPercent = percent.format(incPercent);
        }

        public boolean contains(PriorityStackTraceProfile priorityStackTraceProfile) {
            if (timeCounter.getTotalCount() == priorityStackTraceProfile.timeCounter.getTotalCount()
                    && incPercent.equals(priorityStackTraceProfile.incPercent)) {
                if (stackString.contains(priorityStackTraceProfile.stackString)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int compareTo(PriorityStackTraceProfile another) {
            int countDiff = (int) (another.timeCounter.getTotalCount() - this.timeCounter.getTotalCount());
            if (countDiff == 0) {
                int anotherStackLength = another.stackString.length();
                int thisStackLength = this.stackString.length();
                if (anotherStackLength > thisStackLength) {
                    return 1;
                } else if (anotherStackLength == thisStackLength) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return countDiff;
            }
        }
    }

    private static class WrappedStackTraceElement {
        public StackTraceElement stackTraceElement;
        public String callStackString;

        WrappedStackTraceElement(StackTraceElement stackTraceElement, String callStackString) {
            this.stackTraceElement = stackTraceElement;
            this.callStackString = callStackString;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof WrappedStackTraceElement)) {
                return false;
            }
            WrappedStackTraceElement castObj = (WrappedStackTraceElement) obj;

            if ((stackTraceElement.getMethodName() == null) || (castObj.stackTraceElement.getMethodName() == null)) {
                return false;
            }
            if (!stackTraceElement.getMethodName().equals(castObj.stackTraceElement.getMethodName())) {
                return false;
            }
            if (!stackTraceElement.getClassName().equals(castObj.stackTraceElement.getClassName())) {
                return false;
            }
            String localFileName = stackTraceElement.getFileName();
            if (localFileName == null) {
                if (castObj.stackTraceElement.getFileName() != null) {
                    return false;
                }
            } else {
                if (!localFileName.equals(castObj.stackTraceElement.getFileName())) {
                    return false;
                }
            }

            if (callStackString == null && castObj.callStackString != null) {
                return false;
            }
            if (callStackString != null && !callStackString.equals(castObj.callStackString)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return stackTraceElement.hashCode();
        }
    }

    private static class TimeCounter {
        private int count;
        private List<Long> timeList = new ArrayList<>();

        void addTime(long time) {
            timeList.add(time);
            count++;
        }

        long getTotalTime() {
            long totalTime = 0;
            for (long time : timeList) {
                totalTime += time;
            }
            return totalTime;
        }

        long getTotalCount() {
            return count;
        }

        void reset() {
            count = 0;
            timeList.clear();
        }
    }
}
