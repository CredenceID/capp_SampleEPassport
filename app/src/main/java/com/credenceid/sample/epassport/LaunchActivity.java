package com.credenceid.sample.epassport;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.credenceid.biometrics.Biometrics;
import com.credenceid.biometrics.BiometricsManager;
import com.credenceid.biometrics.DeviceFamily;
import com.credenceid.biometrics.DeviceType;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static com.credenceid.biometrics.Biometrics.ResultCode.OK;

public class LaunchActivity
		extends Activity {

	private static final String TAG = LaunchActivity.class.getSimpleName();

	/* CredenceSDK biometrics object, used to interface with APIs. */
	@SuppressLint("StaticFieldLeak")
	private static BiometricsManager mBiometricsManager;
	/* Stores which Credence family of device's this app is running on. */
	private static DeviceFamily mDeviceFamily = DeviceFamily.InvalidDevice;
	/* Stores which specific device this app is running on. */
	private static DeviceType mDeviceType = DeviceType.InvalidDevice;

	public static BiometricsManager
	getBiometricsManager() {
		return mBiometricsManager;
	}

	@SuppressWarnings("unused")
	public static DeviceFamily
	getDeviceFamily() {
		return mDeviceFamily;
	}

	@SuppressWarnings("unused")
	public static DeviceType
	getDeviceType() {
		return mDeviceType;
	}

	@Override
	protected void
	onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*  Create new biometrics object. */
		mBiometricsManager = new BiometricsManager(this);

		/* Initialize object, meaning tell CredenceService to bind to this application. */
		mBiometricsManager.initializeBiometrics((Biometrics.ResultCode resultCode,
												 String minimumVersion,
												 String currentVersion) -> {
			if (resultCode == OK) {
				Toast.makeText(this, "Biometrics initialized.", LENGTH_SHORT).show();

				mDeviceFamily = mBiometricsManager.getDeviceFamily();
				mDeviceType = mBiometricsManager.getDeviceType();

				/* Launch main activity. */
				Intent intent = new Intent(this, MRZActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
			} else Toast.makeText(this, "Biometrics FAILED to initialize.", LENGTH_LONG).show();
		});
	}
}
