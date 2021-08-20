package me.zhouzhuo810.cameracardcrop;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import me.zhouzhuo810.cameracardcrop.manager.AutoFocusManager;


/**
 * Preview of camera
 *
 * @author zhouzhuo810
 * @date 2017/6/15
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private AutoFocusManager autoFocusManager;
    private boolean released;

    public CameraView(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        try {
            initCamera(mCamera);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCamera(Camera camera) throws Exception{
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : previewSizes) {
            if (size.width / 16 == size.height / 9) {
                parameters.setPreviewSize(size.width, size.height);
                break;
            }
        }
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizes) {
            if (size.width / 16 == size.height / 9) {
                parameters.setPictureSize(size.width, size.height);
                break;
            }
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CameraUtils.findCameraId(false), info);
        int rotation = info.orientation % 360;
        parameters.setRotation(rotation);
        camera.setDisplayOrientation(isLandscape() ? 0 : 90);
        parameters.setJpegQuality(100);
        camera.setParameters(parameters);
    }
    
    /**
     * 是否横屏
     *
     * @return 是/否
     */
    private boolean isLandscape() {
        int screenW = ScreenUtils.getScreenWidth(getContext());
        int screenH = ScreenUtils.getScreenHeight(getContext());
        return screenW > screenH;
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            if (autoFocusManager == null) {
                autoFocusManager = new AutoFocusManager(mCamera);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (getHolder().getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
            if (autoFocusManager != null) {
                autoFocusManager.stop(released);
                autoFocusManager = null;
            }
        } catch (Exception e) {

        }

        try {
            initCamera(mCamera);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            if (autoFocusManager == null) {
                autoFocusManager = new AutoFocusManager(mCamera);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (autoFocusManager != null) {
            autoFocusManager.stop(released);
            autoFocusManager = null;
        }
    }
}
