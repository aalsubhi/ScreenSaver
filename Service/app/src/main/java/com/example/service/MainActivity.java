package com.example.service;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity"; //define constant var for logging
    // Job Schedualer works on API level 21 or above
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ComponentName comonentName = new ComponentName(getApplicationContext(), ExampleJobService.class);
        JobInfo info = new JobInfo.Builder(122, comonentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)  //to keep the service alive even the device is rebooted
                .setPeriodic(15 * 60 * 1000)     //can't go lower than 15 mins if sat lower , it will sat automatically to 15. 15 * 60 * 1000 = time in millisecond
                . build();
        JobScheduler scheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "Job scheduled");

        }

        else{
            Log.d(TAG, "Job scheduling failed");
        }
    }
}
