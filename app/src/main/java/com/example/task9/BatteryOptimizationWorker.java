package com.example.task9;

import android.content.Context;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BatteryOptimizationWorker extends Worker {
    public BatteryOptimizationWorker(Context context, WorkerParameters params) {
        super(context, params); // Pass context and params to the superclass constructor
    }

    @Override
    public Result doWork() {
        // Implement battery optimization logic here
        // Could adjust location update frequency based on battery level
        return Result.success();
    }
}