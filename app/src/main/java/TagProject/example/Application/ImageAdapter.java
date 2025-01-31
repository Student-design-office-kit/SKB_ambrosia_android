package TagProject.example.Application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Класс для обработки изображений
 */
public class ImageAdapter {

    private Context context;
    private Activity activity;

    /**
     * Метод конструктор класса
     */
    public ImageAdapter(Context ctx, Activity activity) {
        context = ctx;
        this.activity = activity;
    }

    /**
     * Метод кодирует изображение из Bitmap в base64 формат
     *
     * @param image - полученное камерой изображение (Bitmap)
     */
    public String encodeImage(Bitmap image) {
        String encodeImage;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        encodeImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodeImage;
    }

    /**
     * Метод конвертирует иконку метки
     * в подходящий для карты формат
     *
     * @param context    - контекст (Context)
     * @param drawableId - id ресурса для конвертации (Integer)
     */
    public static BitmapDescriptor getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    static final int REQUEST_PICTURE_CAPTURE = 1;
    private String pictureFilePath;
    private File pictureFile;
    private File image;

    /**
     * Метод создаёт файл изображения
     * и возвращает его
     */
    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(new Date());
        String pictureFile = timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(pictureFile, ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    /**
     * Метод отправляет запрос на
     * запуск камеры устройства
     */
    public void sendTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, true);
        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                Toast.makeText(context, "Photo file can't be created, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context, "com.TagProject.application" + ".provider", pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }
    }

    /**
     * Метод возвращает Bitmap
     * стандартного размера для сервера
     */
    public Bitmap getBitmap() {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions);
        if (bitmap.getWidth() > bitmap.getHeight())
            bitmap = Bitmap.createScaledBitmap(bitmap, 1920, 1080, true);
        else bitmap = Bitmap.createScaledBitmap(bitmap, 1080, 1920, true);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(pictureFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Objects.requireNonNull(exif.getAttribute(ExifInterface.TAG_ORIENTATION)).equalsIgnoreCase("6")) {
            bitmap = rotate(bitmap, 90);
        } else if (Objects.requireNonNull(exif.getAttribute(ExifInterface.TAG_ORIENTATION)).equalsIgnoreCase("8")) {
            bitmap = rotate(bitmap, 270);
        } else if (Objects.requireNonNull(exif.getAttribute(ExifInterface.TAG_ORIENTATION)).equalsIgnoreCase("3")) {
            bitmap = rotate(bitmap, 180);
        }

        image.delete();
        return bitmap;
    }

    public Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
}