package com.philliphsu.clock2.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.ringtone.RingtoneService;
import com.philliphsu.clock2.util.AlarmController;
import com.philliphsu.clock2.util.AlarmUtils;

import static com.philliphsu.clock2.util.DateFormatUtils.formatTime;

public class AlarmRingtoneService extends RingtoneService<Alarm> {
    private static final String TAG = "AlarmRingtoneService";
    /* TOneverDO: not private */
    private static final String ACTION_SNOOZE = "com.philliphsu.clock2.ringtone.action.SNOOZE";
    private static final String ACTION_DISMISS = "com.philliphsu.clock2.ringtone.action.DISMISS";

    private String mNormalRingTime;
    private AlarmController mAlarmController;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We can have this before super because this will only call through
        // WHILE this Service has already been alive.
        if (intent.getAction() != null) {
            if (ACTION_SNOOZE.equals(intent.getAction())) {
                mAlarmController.snoozeAlarm(getRingingObject());
            } else if (ACTION_DISMISS.equals(intent.getAction())) {
                mAlarmController.cancelAlarm(getRingingObject(), false); // TODO do we really need to cancel the intent and alarm?
            } else {
                throw new UnsupportedOperationException();
            }
            // ==========================================================================
            stopSelf(startId);
            finishActivity();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmController = new AlarmController(this, null);
    }

    @Override
    protected void onAutoSilenced() {
        // TODO do we really need to cancel the alarm and intent?
        mAlarmController.cancelAlarm(getRingingObject(), false);
        // Post notification that alarm was missed
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.missed_alarm))
                .setContentText(mNormalRingTime)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        nm.notify(TAG, getRingingObject().intId(), note);
    }

    @Override
    protected Uri getRingtoneUri() {
        return Uri.parse(getRingingObject().ringtone());
    }

    @Override
    protected Notification getForegroundNotification() {
        String title = getRingingObject().label().isEmpty()
                ? getString(R.string.alarm)
                : getRingingObject().label();
        mNormalRingTime = formatTime(this, System.currentTimeMillis()); // now
        return new NotificationCompat.Builder(this)
                // Required contents
                .setSmallIcon(R.mipmap.ic_launcher) // TODO: alarm icon
                .setContentTitle(title)
                .setContentText(mNormalRingTime)
                .addAction(R.mipmap.ic_launcher, // TODO: correct icon
                        getString(R.string.snooze),
                        getPendingIntent(ACTION_SNOOZE, getRingingObject().getIntId()))
                .addAction(R.mipmap.ic_launcher, // TODO: correct icon
                        getString(R.string.dismiss),
                        getPendingIntent(ACTION_DISMISS, getRingingObject().getIntId()))
                .build();
    }

    @Override
    protected boolean doesVibrate() {
        return getRingingObject().vibrates();
    }

    @Override
    protected int minutesToAutoSilence() {
        return AlarmUtils.minutesToSilenceAfter(this);
    }
}