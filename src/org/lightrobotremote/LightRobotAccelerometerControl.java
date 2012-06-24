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
	
	//Fields for the acc data
	private float mData_acc_x;
	private float mData_acc_y;

	public LightRobotAccelerometerControl(SensorManager sensorManager, WindowManager windowManager, Handler handler)
	{//TODO:: Add data manager and dont communicate with the activity directly!
		mSensorManager = sensorManager;
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mDisplay = windowManager.getDefaultDisplay();
		
		mHandler = handler;
		
		//mDataManager = dataManager;
		
		mData_acc_x = 0.f;
		mData_acc_y = 0.f;
		
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
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
		
		mData_acc_x = sensorX;
		mData_acc_y = sensorY;
		
		mHandler.obtainMessage(LightRobotRemoteInterface.MESSAGE_UPDATE_DATA,0,0).sendToTarget();

	}
	
	public float getSensorX()
	{
		return mData_acc_x;
	}
	
	public float getSensorY()
	{
		return mData_acc_y;
	}


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// nothing to do here
	}

}
