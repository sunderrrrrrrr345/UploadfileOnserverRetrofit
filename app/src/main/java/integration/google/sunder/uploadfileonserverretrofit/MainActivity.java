package integration.google.sunder.uploadfileonserverretrofit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.drm.ProcessedData;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity implements ProgressRequestBody.UploadCallbacks{
 //   ProgressDialog dialog;
    ProgressBar progressBar;
    TextView upload;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1, REQUEST_CAMERA_ID = 2, SELECT_FILE_ID = 3, REQUEST_CAMERA1 = 4;
    private String userChoosenTask;
    private Bitmap thumbnail1;
    byte[] b1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        upload=findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"Take Photo", "Choose from Gallery", "Cancel"};
                AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                build.setTitle("Add Photo");
                build.setCancelable(false);
                build.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean result = Utility.checkPermission(MainActivity.this);
                        if (items[which].equals("Take Photo")) {
                            userChoosenTask = "Take Photo";
                            if (result)
                                cameraIntent();

                        } else if (items[which].equals("Choose from Gallery")) {
                            userChoosenTask = "Choose from Library";
                            if (result)
                                galleryIntent();
                        } else if (items[which].equals("Cancel")) {
                            userChoosenTask = "Cancel";
                            dialog.dismiss();
                        }

                    }
                });
                AlertDialog alert = build.create();
                alert.show();
            }
        });



    }

    private void galleryIntent() {
    }

    private void cameraIntent() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        5);

            }
        } else {
            //  Toast.makeText(getActivity(), "Gallery111", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    // Open your camera here.
                    onCaptureImageResult(data);
                    Log.i("resultCode", "resultCode:" + "" + data);
                    break;
                case 1:
                    // Open your camera here.
                    onSelectFromGalleryResult(data);
                    Log.i("resultCode", "resultCode:" + "" + data);
                    break;
            }

        }
    }

    private void onCaptureImageResult(Intent data) {
        thumbnail1 = (Bitmap) data.getExtras().get("data");
        Log.i("requestFile2", ""+data.getData());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail1.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        b1 = bytes.toByteArray();
        String  encodedImageonCapture = Base64.encodeToString(b1, Base64.DEFAULT);
       // GalleryimageUpload(encodedImageonCapture);
        Upload_documents(data);
    }

    private void Upload_documents(Intent encodedImageonCapture) {
        //  dialog = new ProgressDialog(MainActivity.this);
        //  dialog.setCancelable(false);
        //  dialog.setMessage("Uploading");
        File file = new File(encodedImageonCapture.getExtras().get("data").toString());
        //   dialog.setProgress(0);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIUrl.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
   /*   dialog.setMessage("Uploading " + file.getName()+"/"+file.length());
        dialog.show();*/
       Api as = retrofit.create(Api.class);
       RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), encodedImageonCapture.getExtras().get("data").toString());
        //ProgressRequestBody fileBody = new ProgressRequestBody(file, this);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Log.i("requestFile", requestFile.toString());
        Log.i("requestFile1", "" + multipartBody.toString());
        Log.i("requestFile1", "" + file.getName());
        Call<ResponseBody> responseBodyCall = as.addRecord("afddf", 1325, "fileName", multipartBody);
        Log.i("requestFile1", "" + responseBodyCall.toString());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.d("Success", "success " + response.code());
                Log.d("Success", "success " + response.message());
                Toast.makeText(getApplicationContext(),"yes",Toast.LENGTH_SHORT).show();
                //    dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("failure", "message = " + t.getMessage());
                Log.d("failure", "cause = " + t.getCause());
                Toast.makeText(getApplicationContext(),"no",Toast.LENGTH_SHORT).show();
                //   dialog.dismiss();
            }
        });
    }

    private void onSelectFromGalleryResult(Intent data) {
 
    }

    @Override
    public void onProgressUpdate(int percentage) {
        progressBar.setProgress(percentage);
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {
        progressBar.setProgress(100);
    }
}
