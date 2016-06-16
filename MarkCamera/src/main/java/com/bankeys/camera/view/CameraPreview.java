package com.bankeys.camera.view;

import java.io.IOException;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * 实现自定义相机
 * 
 * @author LiuPeng
 * 
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	// private static final String TAG = "CameraPreview";

	/** 分辨率 */
	/** 监听接口 */
	private OnCameraStatusListener listener;

	private SurfaceHolder holder;
	private Camera camera;

	// 创建一个PictureCallback对象，并实现其中的onPictureTaken方法
	private PictureCallback pictureCallback = new PictureCallback() {

		// 该方法用于处理拍摄后的照片数据
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			height = camera.getParameters().getPictureSize().height;
			width = camera.getParameters().getPictureSize().width;
			// 停止照片拍摄
			camera.stopPreview();
			camera = null;

			// 调用结束事件
			if (null != listener) {
				listener.onCameraStopped(data, width, height);
			}
		}
	};

	private int width;
	private int height;

	// Preview类的构造方法
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 获得SurfaceHolder对象
		holder = getHolder();
		// 指定用于捕捉拍照事件的SurfaceHolder.Callback对象
		holder.addCallback(this);
		// 设置SurfaceHolder对象的类型
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void startPreview() {
		camera.startPreview();
	}

	public void stopPreView() {
		camera.stopPreview();
	}

	// 在surface创建时激发
	public void surfaceCreated(SurfaceHolder holder) {
		// Log.e(TAG, "==surfaceCreated==");
		camera = Camera.open();
		try {
			// 设置用于显示拍照摄像的SurfaceHolder对象
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
			camera.release();
			camera = null;
		} catch (RuntimeException e) {
			//camera = Camera.open(Camera.getNumberOfCameras() - 1);
			Toast.makeText(getContext(), "请在权限管理里查看是否禁用拍照权限！",Toast.LENGTH_LONG).show();
		}
	}

	// 在surface销毁时激发
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Log.e(TAG, "==surfaceDestroyed==");
		camera.release();
	}

	// 在surface的大小发生改变时激发
	public void surfaceChanged(final SurfaceHolder holder, int format, int w, int h) {
		// Log.e(TAG, "==surfaceChanged==");
		try {
			// 获取照相机参数
			Camera.Parameters parameters = camera.getParameters();
			// 设置照片格式
			parameters.setPictureFormat(PixelFormat.JPEG);
			// parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			// // 设置预浏尺寸
			// parameters.setPreviewSize(WIDTH, HEIGHT);
			// 设置照片分辨率

			width = parameters.getPreviewSize().width;
			height = parameters.getPreviewSize().height;

			camera.setParameters(parameters);
			// 开始拍照
			camera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 停止拍照，并将拍摄的照片传入PictureCallback接口的onPictureTaken方法
	public void takePicture() {
		// Log.e(TAG, "==takePicture==");
		if (camera != null) {

			camera.takePicture(null, null, pictureCallback);
			// // �Զ��Խ�
			// camera.autoFocus(new AutoFocusCallback() {
			// @Override
			// public void onAutoFocus(boolean success, Camera
			// camera) {
			// if (null != listener) {
			// listener.onAutoFocus(success);
			// }
			// // �Զ��Խ��ɹ��������
			// if (success) {
			//
			// }
			// }
			// });
		}
	}

	// 设置监听事件
	public void setOnCameraStatusListener(OnCameraStatusListener listener) {
		this.listener = listener;
	}

	/**
	 * 相机拍照监听事件
	 */
	public interface OnCameraStatusListener {

		// 相机拍照结束事件
		void onCameraStopped(byte[] data, int wigth, int height);

		// 拍摄时自动对焦功能
		void onAutoFocus(boolean success);
	}

}