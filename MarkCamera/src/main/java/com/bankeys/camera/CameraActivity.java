package com.bankeys.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bankeys.camera.view.CameraPreview;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * 拍摄身份证正反面 TODO
 *
 * @author LiuPeng
 */
public class CameraActivity extends Activity {

    private Dialog mDialog;
    private CameraPreview mCameraPreview;
    private Button btn, ok, cancle;
    private RelativeLayout savePreview;
    // 用来生成base64
    private byte[] photo;
    // 用来生成缩略图
    Bitmap photobitmp;
    public static final String RESULT_TAKEPHOTO = "result_takePhoto";
    public static final String REQUEST_WIDTH = "request_width";
    public static final String REQUEST_HEIGHT = "request_height";
    private float width;
    private float height;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              //  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_camera_activity);
        init();
    }

    private void init() {
        width = getIntent().getFloatExtra(REQUEST_WIDTH, 480);
        height = getIntent().getFloatExtra(REQUEST_HEIGHT, 320);

        mCameraPreview = (CameraPreview) findViewById(R.id.preview);
        mCameraPreview.setOnCameraStatusListener(new sOnCameraStatusListener());
        // mCameraPreview.getViewTreeObserver().addOnPreDrawListener(new
        // AutoSizeListener(mCameraPreview, 2));
        btn = (Button) findViewById(R.id.takePhoto);
        btn.getViewTreeObserver().addOnPreDrawListener(new AutoSizeListener(btn));

        ok = (Button) findViewById(R.id.ok);
        cancle = (Button) findViewById(R.id.cancle);
        ok.getViewTreeObserver().addOnPreDrawListener(new AutoSizeListener(ok));
        cancle.getViewTreeObserver().addOnPreDrawListener(new AutoSizeListener(cancle));
        savePreview = (RelativeLayout) findViewById(R.id.save_preview);
        MyClickLis cls = new MyClickLis();
        btn.setOnClickListener(cls);
        ok.setOnClickListener(cls);
        cancle.setOnClickListener(cls);
        setResult(RESULT_CANCELED);
    }

    private class MyClickLis implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ok) {
                showWaitingDialog(R.string.wait_savePhoto, true).show();
                savePicture();
            } else if (v.getId() == R.id.cancle) {
                mCameraPreview.startPreview();
                savePreview.setVisibility(View.GONE);
                btn.setVisibility(View.VISIBLE);
            } else if (v.getId() == R.id.takePhoto) {
                // mCameraPreview.restartCamera();
                showWaitingDialog(R.string.wait_takePhoto, true).show();
                btn.setVisibility(View.GONE);
                mCameraPreview.takePicture();
            }
        }
    }

    ;

    /**
     * 得到SD卡路径，如果没有SD卡，则返回certPath
     *
     * @return
     */
    public String getSdCardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
        } else {
            sdDir = this.getFilesDir();
        }
        return sdDir.toString();
    }

    private void savePicture() {
        showWaitingDialog(R.string.wait_savePhoto, true);
        Log.e(getPackageName(), "================save============");
        Intent intent = new Intent();
        intent.putExtra(RESULT_TAKEPHOTO, photo);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private class sOnCameraStatusListener implements CameraPreview.OnCameraStatusListener {
        /**
         * 相机拍照结束
         */
        @Override
        public void onCameraStopped(byte[] data, int with, int height) {
            Log.e(getPackageName(), "==with==" + with);
            Log.e(getPackageName(), "==height==" + height);

            Bitmap bit = getimage(data, with, height);

            Bitmap b2t = getMenbanimage();
            Log.e(getPackageName(), bit.getHeight() + "      " + bit.getWidth());
            Log.e(getPackageName(), b2t.getHeight() + "      " + b2t.getWidth());
            Bitmap photobitmp = createBitmap(bit, b2t);
            photo = compressImage(photobitmp);
            // Bitmap bi2t = BitmapFactory.decodeByteArray(photo, 0,
            // photo.length);
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            savePreview.setVisibility(View.VISIBLE);
            // savePreview.setBackgroundDrawable(new
            // BitmapDrawable(bi2t));
        }

        /**
         * 拍摄时自动对焦
         */
        @Override
        public void onAutoFocus(boolean success) {
            // 改变对焦状态图像
            if (success) {
            } else {
                Toast.makeText(CameraActivity.this, "焦距不准，请重拍！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 图片质量压缩至50K
     *
     * @param image
     * @return
     */
    private byte[] compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//
        int options = 100;
        while (baos.toByteArray().length / 1024 > 50) {
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 5;// 每次都减少5
        }
        return baos.toByteArray();
    }

    /**
     * ͼ
     *
     * @param src
     * @param watermark
     * @return
     */
    private Bitmap createBitmap(Bitmap src, Bitmap watermark) {
        if (src == null) {
            return null;
        }
        if (watermark == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        // create the new blank bitmap
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个跟src长宽一致的位图
        Canvas cv = new Canvas(newb);
        cv.drawBitmap(src, 0, 0, null);// 画入SRC
        cv.drawBitmap(watermark, 0, 0, null);//
        cv.save(Canvas.ALL_SAVE_FLAG);// 保存图片
        cv.restore();// 存储
        return newb;
    }

    private Bitmap getimage(byte[] src, int with, int height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = false;
        int w = with;
        int h = height;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1��ʾ������
        if (w > h && w > this.width) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (with / this.width);
        } else if (w < h && h > this.height) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (height / this.height);
        }
        if (height / this.height > w / this.width) {
            be = (int) (h / this.height);
        }

        if (be <= 0)
            be = 1;
        Log.e(getPackageName(), "be is :工" + be);
        newOpts.inSampleSize = be;//
        newOpts.outHeight = 480;
        newOpts.outWidth = 800;
        Bitmap bitmp = BitmapFactory.decodeByteArray(src, 0, src.length, newOpts);

        float scaleWidth = ((float) 800) / bitmp.getWidth();
        float scaleHeight = ((float) 480) / bitmp.getHeight();
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmp, 0, 0, bitmp.getWidth(), bitmp.getHeight(), matrix,
                false);

        Log.e(getPackageName(), "bitmat with = " + resizedBitmap.getWidth());
        Log.e(getPackageName(), "bitmat height = " + resizedBitmap.getHeight());
        return resizedBitmap;
    }

    /**
     * 图片长宽改为 800 480
     *
     * @return
     */
    private Bitmap getMenbanimage() {

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_camera_seal);// ��ʱ����bmΪ��
        newOpts.inJustDecodeBounds = false;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 480f;
        float ww = 800f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1
        if (w > h && w > ww) {//
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//
        Matrix matrix = new Matrix();
        matrix.postScale(ww / w, hh / h);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        Log.d("CameraActivity", resizedBitmap.getWidth() + ";" + resizedBitmap.getHeight());
        return resizedBitmap;
    }

    /**
     * 进度条 对话框
     *
     * @param title    标题
     * @param isCancel 是否可以被用户取消
     */
    public ProgressDialog showWaitingDialog(int title, boolean isCancel) {
        mDialog = new ProgressDialog(this);
        ((ProgressDialog) mDialog).setProgress(ProgressDialog.STYLE_HORIZONTAL);
        ((AlertDialog) mDialog).setMessage(getResources().getString(title));
        mDialog.setCancelable(isCancel);
        return (ProgressDialog) mDialog;
    }
}

