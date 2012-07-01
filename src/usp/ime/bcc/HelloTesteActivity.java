package usp.ime.bcc;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class HelloTesteActivity extends Activity implements LocationListener, SensorEventListener {


	private final static float VARIACAO_ANGULAR = (float) 1.0;
	
	private LocationManager lm;
	private TextView tv;
	private TextView tv2;
	private TextView tv3;
	/*private TextView tv4;
	private TextView tv5;
	private TextView tv6;*/
	private TextView tv7;
	private TextView tv8;
	private int cont = 0;

	FileWriter fileOutput;
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
	
	private SensorManager mSensorManager;
	private float[] gravity = new float[3];
	private float[] lastGeomag = new float[3];
	private boolean firstGeomag = true;
	private float[] geomag = new float[3];
	private float[] rotationMatrix;
	private float[] originalGravity = new float[3];
	private float[] originalGeoMagnetic = new float[3];
	private float[] originalRotationMatrix;
	
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        tv = (TextView) findViewById(R.id.label1);
	        tv2 = (TextView) findViewById(R.id.label2);
	        tv3 = (TextView) findViewById(R.id.label3);
	        /*tv4 = (TextView) findViewById(R.id.label4);
	        tv5 = (TextView) findViewById(R.id.label5);
	        tv6 = (TextView) findViewById(R.id.label6);*/
	        tv7 = (TextView) findViewById(R.id.label7);
	        tv8 = (TextView) findViewById(R.id.label8);

	        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);
	        
	        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	        
	        try{
				fileOutput = new FileWriter("/sdcard/TCC/coleta.txt", false);
	        }
	        catch(Exception e){
	        	e.printStackTrace();
	        }
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

	    public void onLocationChanged(Location location) {
	        String lat = String.valueOf(location.getLatitude());
	        String lon = String.valueOf(location.getLongitude());
	        
	        String s = "L: " + lat + ", " + lon + ", " + dateFormat.format(new Date()) + "\n";
	        try {
	        	synchronized (fileOutput) {
	        		fileOutput.write(s);
		    		fileOutput.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        Log.e("GPS", "location changed: " + s);
	        tv.setText(s);
	    }
	    public void onProviderDisabled(String arg0) {
	        Log.e("GPS", "provider disabled " + arg0);
	    }
	    public void onProviderEnabled(String arg0) {
	        Log.e("GPS", "provider enabled " + arg0);
	    }
	    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	        Log.e("GPS", "status changed to " + arg0 + " [" + arg1 + "]");
	    }

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}
		

		public void onSensorChanged(SensorEvent evt) {
			int type=evt.sensor.getType();
		    
		    //Smoothing the sensor data a bit
		    if (type == Sensor.TYPE_MAGNETIC_FIELD) {
		    	originalGeoMagnetic[0] = evt.values[0];
		    	originalGeoMagnetic[1] = evt.values[1];
		    	originalGeoMagnetic[2] = evt.values[2];
		      geomag[0]=(geomag[0]*1+evt.values[0])*0.5f;
		      geomag[1]=(geomag[1]*1+evt.values[1])*0.5f;
		      geomag[2]=(geomag[2]*1+evt.values[2])*0.5f;
		      
		      if( firstGeomag || Math.abs(lastGeomag[0]-geomag[0]) > VARIACAO_ANGULAR 
		    		  || Math.abs(lastGeomag[1]-geomag[1]) > VARIACAO_ANGULAR
		    		  || Math.abs(lastGeomag[2]-geomag[2]) > VARIACAO_ANGULAR)
		      {
		    	  lastGeomag[0] = geomag[0];
		    	  lastGeomag[1] = geomag[1];
		    	  lastGeomag[2] = geomag[2];
		    	 
		    	  Log.e("GPS", "deomag changed: " + geomag);
		    	  String g = "g: " + geomag[0] + ", " + geomag[1] + ", " + geomag[2] + "," + dateFormat.format(new Date()) + "\n";
		    	  String G = "G: " + originalGeoMagnetic[0] + ", " + originalGeoMagnetic[1] + ", " + originalGeoMagnetic[2] + "," + dateFormat.format(new Date()) + "\n";
				  tv2.setText(g);
				  tv3.setText(G);
				  firstGeomag = false;
				  
				  try {
					  	String s = g + G;
					  	synchronized (fileOutput) {
					  		fileOutput.write(s);
				    		fileOutput.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
		      }
		      else{
		    	  return;
		      }
		    } 
		    
		    if (type == Sensor.TYPE_ACCELEROMETER) {
		    	originalGravity[0] = evt.values[0];
		    	originalGravity[1] = evt.values[1];
		    	originalGravity[2] = evt.values[2];
		    	float alpha = 2/3;
				gravity[0] = gravity[0]*alpha + (1 - alpha)*evt.values[0];
				gravity[1] = gravity[1]*alpha + (1 - alpha)*evt.values[1];
				gravity[2] = gravity[2]*alpha + (1 - alpha)*evt.values[2];
				
				/*tv3.setText("g: " + gravity[0] + ", " + gravity[1] + ", " + gravity[2] + "," + new Date().getTime());
			    tv6.setText("G: [" + originalGravity[0] + ", " + originalGravity[1] + ", " + originalGravity[2] + "]");*/				
		    }
		    

		    if ((type==Sensor.TYPE_MAGNETIC_FIELD)/* || (type==Sensor.TYPE_ACCELEROMETER)*/) {
		      rotationMatrix = new float[16];
		      float[] orientation = new float[3];
		      float[] originalOrientation = new float[3];
		      originalRotationMatrix = new float[16];

		      SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomag);
		      SensorManager.getOrientation(rotationMatrix, orientation);
		      String o = "o: " + orientation[0]*180/Math.PI + ", " + orientation[1]*180/Math.PI + ", " + orientation[2]*180/Math.PI + "," + dateFormat.format(new Date()) + "\n";
		      tv7.setText(o);
		      
		      SensorManager.remapCoordinateSystem( 
		        rotationMatrix, 
		        SensorManager.AXIS_Y, 
		        SensorManager.AXIS_MINUS_X, 
		        rotationMatrix );
		      
		      SensorManager.getRotationMatrix(originalRotationMatrix, null, originalGravity, originalGeoMagnetic);
		      SensorManager.getOrientation(originalRotationMatrix, originalOrientation);
		      String O = "O: " + originalOrientation[0]*180/Math.PI + ", " + originalOrientation[1]*180/Math.PI + ", " + originalOrientation[2]*180/Math.PI + "," + dateFormat.format(new Date()) + "\n";
		      tv8.setText(O);
		      SensorManager.remapCoordinateSystem( 
				        originalRotationMatrix, 
				        SensorManager.AXIS_Y, 
				        SensorManager.AXIS_MINUS_X, 
				        originalRotationMatrix );
		      
		      try {
				  	String s = o + O;
				  	synchronized (fileOutput) {
				  		fileOutput.write(s);
			    		fileOutput.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		}
}






















