package org.moegirl.moegirlview;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;

/**
 * 用于检测手机摇晃
 * 
 * @author 郑智仁
 * @see <a href="http://blog.csdn.net/zhengzhiren">Blog</a>
 */
public class ShakeDetector implements SensorEventListener {

	/**
	 * 检测的时间间隔
	 */
	private static final int UPDATE_INTERVAL = 100;

	/**
	 * 当检测到一次摇晃发生，与下次开始检测的间隔时间（毫秒）
	 */
	private static final long SHAKE_INTERVAL = 500;

	/**
	 * 是否首次检测。如果是首次检测，要对mLastX, mLastY, mLastZ等进行初始化
	 */
	private boolean mFirstUpdate;

	/**
	 * 上一次检测的时间
	 */
	private long mLastUpdateTime;

	/**
	 * 上一次发生摇晃的时间
	 */
	private long mLastShakeTime = 0;

	/**
	 * 上一次检测时，加速度在x、y、z方向上的分量，用于和当前加速度比较求差。
	 */
	private float mLastX, mLastY, mLastZ;

	private SensorManager mSensorManager;
	private ArrayList<OnShakeListener> mListeners;

	/**
	 * 摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。
	 */
	private int mShakeThreshold = 3000;

	/**
	 * 摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。
	 * 
	 * @return
	 */
	public int getShakeThreshold() {
		return mShakeThreshold;
	}

	/**
	 * 摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。
	 */
	public void setShakeThreshold(int threshold) {
		mShakeThreshold = threshold;
	}

	public ShakeDetector(Context context) {
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mListeners = new ArrayList<OnShakeListener>();
	}

	/**
	 * 当摇晃事件发生时，接收通知
	 */
	public interface OnShakeListener {
		/**
		 * 当手机摇晃时被调用
		 */
		void onShake();
	}

	/**
	 * 注册OnShakeListener，当摇晃时接收通知
	 * 
	 * @param listener
	 */
	public void registerOnShakeListener(OnShakeListener listener) {
		if (!mListeners.contains(listener))
			mListeners.add(listener);
	}

	/**
	 * 移除已经注册的OnShakeListener
	 * 
	 * @param listener
	 */
	public void unregisterOnShakeListener(OnShakeListener listener) {
		mListeners.remove(listener);
	}

	/**
	 * 启动摇晃检测
	 */
	public void start() throws UnsupportedOperationException {
		Sensor sensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (sensor == null) {
			throw new UnsupportedOperationException();
		}
		boolean success = mSensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_GAME);
		if (!success) {
			throw new UnsupportedOperationException();
		}
		mFirstUpdate = true;
	}

	/**
	 * 停止摇晃检测
	 */
	public void stop() {
		if (mSensorManager != null)
			mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		long currentTime = System.currentTimeMillis();
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		long diffTime = currentTime - mLastUpdateTime;

		// 首次检测，进行初始化
		if (mFirstUpdate) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mLastUpdateTime = currentTime;
			mFirstUpdate = false;
			return;
		}

		// 两次检测的间隔
		if (diffTime < UPDATE_INTERVAL) {
			return;
		}

		// 两次摇晃的间隔
		if (currentTime - mLastShakeTime < SHAKE_INTERVAL) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mLastUpdateTime = currentTime;
			return;
		}

		float deltaX = x - mLastX;
		float deltaY = y - mLastY;
		float deltaZ = z - mLastZ;

		mLastX = x;
		mLastY = y;
		mLastZ = z;
		mLastUpdateTime = currentTime;

		// 加速度差值
		float delta = FloatMath.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
				* deltaZ)
				/ diffTime * 10000;

		// 当差值大于指定的阈值，认为这是一个摇晃
		if (delta > mShakeThreshold) {
			mLastShakeTime = currentTime;
			notifyListeners();
		}
	}

	/**
	 * 当摇晃事件发生时，通知所有的listener
	 */
	private void notifyListeners() {
		for (OnShakeListener listener : mListeners) {
			listener.onShake();
		}
	}

}