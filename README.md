# MarkCamera
相机
* 调用
```java
    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
    intent.putExtra(CameraActivity.REQUEST_WIDTH, 480);
    intent.putExtra(CameraActivity.REQUEST_HEIGHT, 320);
    startActivityForResult(intent, 0);
```
 * 结果
```java
   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK && requestCode == 0) {
            byte[] b = data.getByteArrayExtra(CameraActivity.RESULT_TAKEPHOTO);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(b,0,b.length));
        }
    }
```
