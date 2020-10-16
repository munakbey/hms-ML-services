package com.mapp.huawei.text_recogcamera;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.huawei.hms.common.size.Size;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

@SuppressLint("MissingPermission")
public class LensEngine {
    private static final String TAG = "LensEngine";
    private static final int TEXTURE_NAME = 100;

    private Camera camera;
    private final Object transactorLock = new Object();
    private CameraSelector selector;
    private final Map<byte[], ByteBuffer> bytesToByteBuffer = new IdentityHashMap<>();

    private SurfaceTexture surfaceTexture;

    private boolean usingSurfaceTexture;

    public LensEngine(Activity activity, CameraConfiguration configuration) {
        this.selector = new CameraSelector(activity, configuration);
    }

    /**
     * Stop the camera and release the resources of the camera and analyzer.
     */
    public void release() {
        synchronized (this.transactorLock) {
            this.stop();
        }
    }

    /**
     * Turn on the camera and start sending preview frames to the analyzer for detection.
     *
     * @throws IOException IO Exception
     */
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    public synchronized LensEngine run() throws IOException {
        if (this.camera != null) {
            return this;
        }
        this.camera = this.createCamera();
        this.surfaceTexture = new SurfaceTexture(this.TEXTURE_NAME);
        this.camera.setPreviewTexture(this.surfaceTexture);
        this.usingSurfaceTexture = true;
        this.camera.startPreview();
        return this;
    }

    /**
     * Take pictures.
     *
     * @param pictureCallback  Callback function after obtaining photo data.
     */
    public synchronized void takePicture(Camera.PictureCallback pictureCallback) {
        synchronized(this.transactorLock) {
            if (this.camera != null) {
                this.camera.takePicture(null,null,null, pictureCallback);
            }
        }
    }

    public synchronized Camera getCamera() {
        return this.camera;
    }


    /**
     * Get camera preview size.
     *
     * @return Size Size of camera preview.
     */
    public Size getPreviewSize() {
        return this.selector.getPreviewSize();
    }

    public int getFacing() {
        return this.selector.getFacing();
    }

    /**
     * Turn off the camera and stop transmitting frames to the analyzer.
     */
    public synchronized void stop() {
        if (this.camera != null) {
            this.camera.stopPreview();
            this.camera.setPreviewCallbackWithBuffer(null);
            try {
                this.camera.setPreviewDisplay(null);
                if (this.usingSurfaceTexture) {
                    this.camera.setPreviewTexture(null);
                }
            } catch (Exception e) {
                Log.e(LensEngine.TAG, "Failed to clear camera preview: " + e);
            }
            this.camera.release();
            this.camera = null;
        }
        this.bytesToByteBuffer.clear();
    }

    @SuppressLint("InlinedApi")
    private Camera createCamera() throws IOException {
        Camera mCamera = this.selector.createCamera();
        mCamera.addCallbackBuffer(this.createPreviewBuffer(this.selector.getPreviewSize()));
        mCamera.addCallbackBuffer(this.createPreviewBuffer(this.selector.getPreviewSize()));
        mCamera.addCallbackBuffer(this.createPreviewBuffer(this.selector.getPreviewSize()));
        mCamera.addCallbackBuffer(this.createPreviewBuffer(this.selector.getPreviewSize()));
        return mCamera;
    }

    /**
     * Create a buffer for the camera preview callback. The size of the buffer is based on the camera preview size and the camera image format.
     *
     * @param previewSize preview size
     * @return Image data from the camera
     */
    @SuppressLint("InlinedApi")
    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = (long) previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            throw new IllegalStateException("Failed to create valid buffer for lensEngine.");
        }
        this.bytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }
}
