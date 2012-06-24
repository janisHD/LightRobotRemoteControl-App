package org.lightrobotremote;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;


public class LightRobotAccelerometerControl implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	/*!needed to get the orientation of the screen*/
	private Display mDisplay;
	
	private Handler mHandler;
	
	private LightRobotDataManager mDataManager;

	public LightRobotAccelerometerControl(SensorManager sensorManager, WindowManager windowManager, Handler handler, LightRobotDataManager dataManager)
	{
		mSensorManager = sensorManager;
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mDisplay = windowManager.getDefaultDisplay();
		
		mHandler = handler;
		
		mDataManager = dataManager;
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		/*
		 * record the accelerometer data, the event's timestamp as well as
		 * the current time. The latter is needed so we can calculate the
		 * "present" time during rendering. In this application, we need to
		 * take into account how the screen is rotated with respect to the
		 * sensors (which always return data in a coordinate space aligned
		 * to with the screen in its native orientation).
		 */
		
		float sensorX = 0.f;
		float sensorY = 0.f;

		switch (mDisplay.getRotation()) {
		case Surface.ROTATION_0:
			sensorX = event.values[0];
			sensorY = event.values[1];
			break;
		case Surface.ROTATION_90:
			sensorX = -event.values[1];
			sensorY = event.values[0];
			break;
		case Surface.ROTATION_180:
			sensorX = -event.values[0];
			sensorY = -event.values[1];
			break;
		case Surface.ROTATION_270:
			sensorX = event.values[1];
			sensorY = -event.values[0];
			break;
		}
		


	}








	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// nothing to do here
	}

}
