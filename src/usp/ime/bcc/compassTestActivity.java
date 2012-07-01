package usp.ime.bcc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class compassTestActivity extends Activity implements SensorEventListener{

	private SensorManager mSensorManager;
	private float[] gravity = new float[3];
	private float[] geomag = new float[3];
	private float[] rotationMatrix;
	
	TextView view;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		view = new TextView(this);
		
		setContentView(view);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}
	@Override
	protected void onResume() {
		super.onResume();
	    mSensorManager.registerListener(this,
	    		mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
	    		SensorManager.SENSOR_DELAY_GAME );
	    mSensorManager.registerListener(this,
	    		mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
	    		SensorManager.SENSOR_DELAY_GAME );
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	}
	
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent evt) {
		int type=evt.sensor.getType();
	    
	    //Smoothing the sensor data a bit
	    if (type == Sensor.TYPE_MAGNETIC_FIELD) {
	      geomag[0]=(geomag[0]*1+evt.values[0])*0.5f;
	      geomag[1]=(geomag[1]*1+evt.values[1])*0.5f;
	      geomag[2]=(geomag[2]*1+evt.values[2])*0.5f;
	    } else if (type == Sensor.TYPE_ACCELEROMETER) {
	      gravity[0]=(gravity[0]*2+evt.values[0])*0.33334f;
	      gravity[1]=(gravity[1]*2+evt.values[1])*0.33334f;
	      gravity[2]=(gravity[2]*2+evt.values[2])*0.33334f;
	    }
	    
	    view.setText("gravity: " + gravity);
	    
	    if ((type==Sensor.TYPE_MAGNETIC_FIELD) || (type==Sensor.TYPE_ACCELEROMETER)) {
	      rotationMatrix = new float[16];
	      SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomag);
	      SensorManager.remapCoordinateSystem( 
	        rotationMatrix, 
	        SensorManager.AXIS_Y, 
	        SensorManager.AXIS_MINUS_X, 
	        rotationMatrix );
	    } 
		
	}
	

}
