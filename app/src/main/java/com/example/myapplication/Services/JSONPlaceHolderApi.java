package com.example.myapplication.Services;
import com.example.myapplication.Models.Markers;
import com.example.myapplication.Models.PhotoBase64;
import com.example.myapplication.Models.ResponseModel;
import com.example.myapplication.Models.SendModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface JSONPlaceHolderApi {
    @GET("markers")
    Call<ArrayList<Markers>> getAllMarks();

    @POST("markers/upload/")
    Call<ResponseModel> uploadMarker(@Body SendModel sendModel);
}
