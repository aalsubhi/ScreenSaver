package com.example.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

//we should add the service to manifest file
public class ExampleJobService extends JobService {
    private static final String TAG = "ExampleJobService"; //define constant var for logging
    private boolean jobCacelled = false;
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG,"Job started");
        doBackgroundWork(params);
        return true;
    }
    private void doBackgroundWork (final JobParameters params){
       new Thread(new Runnable() {
           @Override
           public void run() {
               for (int i=0; i<10; i++)
               {
                   Log.d(TAG, "run: "+i);
                   if (jobCacelled)
                   {
                       return;
                   }
                   try {
                       Thread.sleep(1000); //slep for a second
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }

               Log.d(TAG, "Job finished");
               jobFinished(params,true); // put true for rescheduale when somthing went wrong and the job did not complete

           }
       }).start();

    }



    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCacelled = true;
        return true;
    }



}
