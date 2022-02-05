package com.example.myapplication.Services;
import com.example.myapplication.Models.Markers;
import com.example.myapplication.Models.PhotoBase64;
import com.example.myapplication.Models.ResponseModel;
import com.example.myapplication.Models.SendModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface JSONPlaceHolderApi {
    @GET("get_all_markers")
    Call<Markers> getAllMarks();

    @GET("marker/{photo_base64}/get_photo")
    Call<PhotoBase64> getImageInBase64(@Path("photo_base64") String photo_base64);

    @POST("upload")
    Call<ResponseModel> uploadMarker(@Body SendModel sendModel);
}
