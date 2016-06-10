/*
 * Copyright (C) 2011-2014 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.example.ljd.myscreencaptureservice.rtspserver;

import java.io.IOException;
import java.net.InetAddress;

import com.example.ljd.myscreencaptureservice.rtspserver.audio.AACStream;
import com.example.ljd.myscreencaptureservice.rtspserver.audio.AMRNBStream;
import com.example.ljd.myscreencaptureservice.rtspserver.audio.AudioQuality;
import com.example.ljd.myscreencaptureservice.rtspserver.audio.AudioStream;
import com.example.ljd.myscreencaptureservice.rtspserver.gl.SurfaceView;
import com.example.ljd.myscreencaptureservice.rtspserver.video.H263Stream;
import com.example.ljd.myscreencaptureservice.rtspserver.video.H264Stream;
import com.example.ljd.myscreencaptureservice.rtspserver.video.VideoQuality;
import com.example.ljd.myscreencaptureservice.rtspserver.video.VideoStream;
import android.content.Context;
import android.hardware.Camera.CameraInfo;
import android.media.projection.MediaProjection;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Call {@link #getInstance()} to get access to the SessionBuilder.
 */
//用于设置videoStream
public class SessionBuilder {

	public final static String TAG = "SessionBuilder";

	/** Can be used with {@link #setVideoEncoder}. */
	public final static int VIDEO_NONE = 0;

	/** Can be used with {@link #setVideoEncoder}. */
	public final static int VIDEO_H264 = 1;

	/** Can be used with {@link #setVideoEncoder}. */
	public final static int VIDEO_H263 = 2;

	/** Can be used with {@link #setAudioEncoder}. */
	public final static int AUDIO_NONE = 0;

	/** Can be used with {@link #setAudioEncoder}. */
	public final static int AUDIO_AMRNB = 3;

	/** Can be used with {@link #setAudioEncoder}. */
	public final static int AUDIO_AAC = 5;

	// Default configuration
	private VideoQuality mVideoQuality = VideoQuality.DEFAULT_VIDEO_QUALITY;
	private AudioQuality mAudioQuality = AudioQuality.DEFAULT_AUDIO_QUALITY;
	private Context mContext;
	private int mVideoEncoder = VIDEO_H263; 
	private int mAudioEncoder = AUDIO_AMRNB;
	private int mCamera = CameraInfo.CAMERA_FACING_BACK;
	private int mTimeToLive = 64;
	private int mOrientation = 0;
	private boolean mFlash = false;
	private SurfaceView mSurfaceView = null;
	private String mOrigin = null;
	private String mDestination = null;
	private Session.Callback mCallback = null;
	private MediaProjection mMediaProjection = null;
	public Session session;

	// Removes the default public constructor
	private SessionBuilder() {}

	// The SessionManager implements the singleton pattern
	private static volatile SessionBuilder sInstance = null; 

	/**
	 * Returns a reference to the {@link SessionBuilder}.
	 * @return The reference to the {@link SessionBuilder}
	 */
	//sessionBuilder的单利模式，一个sessionBuilder的实例
	public final static SessionBuilder getInstance() {
		if (sInstance == null) {
			synchronized (SessionBuilder.class) {
				if (sInstance == null) {
					SessionBuilder.sInstance = new SessionBuilder();
				}
			}
		}
		return sInstance;
	}



	/**
	 * Creates a new {@link Session}.
	 * @return The new Session
	 * @throws IOException 
	 */
	//实例化了videoStream,并对videoStream各种设置，不过还没启动videoStream
	public Session build() {
		Log.v(TAG,"Session build()");
		session = new Session();
		session.setOrigin(mOrigin);
		session.setDestination(mDestination);
		session.setTimeToLive(mTimeToLive);
		session.setCallback(mCallback);


		H264Stream stream = new H264Stream(mCamera);
		if (mContext!=null)
			stream.setPreferences(PreferenceManager.getDefaultSharedPreferences(mContext));
		session.addVideoTrack(stream);


		if (session.getVideoTrack()!=null) {
			Log.v(TAG,"video.set......");
			VideoStream video = session.getVideoTrack();
			//VideoStream video = stream;
			video.setFlashState(mFlash);
			video.setVideoQuality(mVideoQuality);
			//video.setSurfaceView(mSurfaceView);
			video.setMediaProjection(mMediaProjection);
			video.setPreviewOrientation(mOrientation);
			video.setDestinationPorts(5006);
		}

		return session;

	}

	/** 
	 * Access to the context is needed for the H264Stream class to store some stuff in the SharedPreferences.
	 * Note that you should pass the Application context, not the context of an Activity.
	 **/
	public SessionBuilder setContext(Context context) {
		mContext = context;
		return this;
	}

	/** Sets the destination of the session. */
	public SessionBuilder setDestination(String destination) {
		mDestination = destination;
		return this; 
	}

	/** Sets the origin of the session. It appears in the SDP of the session. */
	public SessionBuilder setOrigin(String origin) {
		mOrigin = origin;
		return this;
	}

	/** Sets the video stream quality. */
	public SessionBuilder setVideoQuality(int resX, int resY, int framerate, int bitrate,int resScreenDensity) {
		mVideoQuality.framerate = framerate;
		mVideoQuality.bitrate = bitrate;
		mVideoQuality.resX = resX;
		mVideoQuality.resY = resY;
		mVideoQuality.resScreenDensity = resScreenDensity;
		//mVideoQuality = quality.clone();
		return this;
	}
	public SessionBuilder setVideoQuality(VideoQuality quality) {
		mVideoQuality = quality;
		return this;
	}

	/** Sets the audio encoder. */
	public SessionBuilder setAudioEncoder(int encoder) {
		mAudioEncoder = encoder;
		return this;
	}
	
	/** Sets the audio quality. */
	public SessionBuilder setAudioQuality(AudioQuality quality) {
		mAudioQuality = quality.clone();
		return this;
	}

	/** Sets the default video encoder. */
	public SessionBuilder setVideoEncoder(int encoder) {
		mVideoEncoder = encoder;
		return this;
	}

	public SessionBuilder setFlashEnabled(boolean enabled) {
		mFlash = enabled;
		return this;
	}

	public SessionBuilder setCamera(int camera) {
		mCamera = camera;
		return this;
	}

	public SessionBuilder setTimeToLive(int ttl) {
		mTimeToLive = ttl;
		return this;
	}

	/** 
	 * Sets the SurfaceView required to preview the video stream. 
	 **/
	public SessionBuilder setSurfaceView(SurfaceView surfaceView) {
		mSurfaceView = surfaceView;
		return this;
	}

	/*
	 * Sets the MediaProjection
	 */

	public SessionBuilder setMediaProjection(MediaProjection mediaProjection){
		mMediaProjection = mediaProjection;
		return this;
	}

	
	/** 
	 * Sets the orientation of the preview.
	 * @param orientation The orientation of the preview
	 */
	public SessionBuilder setPreviewOrientation(int orientation) {
		mOrientation = orientation;
		return this;
	}	
	
	public SessionBuilder setCallback(Session.Callback callback) {
		mCallback = callback;
		return this;
	}
	public Session getSession(){
		return session;
	}
	/** Returns the context set with {@link #setContext(Context)}*/
	public Context getContext() {
		return mContext;	
	}

	/** Returns the destination ip address set with {@link #setDestination(String)}. */
	public String getDestination() {
		return mDestination;
	}

	/** Returns the origin ip address set with {@link #setOrigin(String)}. */
	public String getOrigin() {
		return mOrigin;
	}

	/** Returns the audio encoder set with {@link #setAudioEncoder(int)}. */
	public int getAudioEncoder() {
		return mAudioEncoder;
	}

	/** Returns the id of the {@link android.hardware.Camera} set with {@link #setCamera(int)}. */
	public int getCamera() {
		return mCamera;
	}

	/** Returns the video encoder set with {@link #setVideoEncoder(int)}. */
	public int getVideoEncoder() {
		return mVideoEncoder;
	}

	/** Returns the VideoQuality set with {@link #setVideoQuality(VideoQuality)}. */
	public VideoQuality getVideoQuality() {
		return mVideoQuality;
	}
	
	/** Returns the AudioQuality set with {@link #setAudioQuality(AudioQuality)}. */
	public AudioQuality getAudioQuality() {
		return mAudioQuality;
	}

	/** Returns the flash state set with {@link #setFlashEnabled(boolean)}. */
	public boolean getFlashState() {
		return mFlash;
	}

	/** Returns the SurfaceView set with {@link #setSurfaceView(SurfaceView)}. */
	public SurfaceView getSurfaceView() {
		return mSurfaceView;
	}
	
	
	/** Returns the time to live set with {@link #setTimeToLive(int)}. */
	public int getTimeToLive() {
		return mTimeToLive;
	}

	/** Returns a new {@link SessionBuilder} with the same configuration. */
	public SessionBuilder clone() {
		return new SessionBuilder()
		.setDestination(mDestination)
		.setOrigin(mOrigin)
		.setSurfaceView(mSurfaceView)
		.setPreviewOrientation(mOrientation)
		.setVideoQuality(mVideoQuality)
		.setVideoEncoder(mVideoEncoder)
		.setFlashEnabled(mFlash)
		.setCamera(mCamera)
		.setTimeToLive(mTimeToLive)
		.setAudioEncoder(mAudioEncoder)
		.setAudioQuality(mAudioQuality)
		.setContext(mContext)
		.setCallback(mCallback);
	}

}