package TagProject.example.myapplication.Services;
import TagProject.example.myapplication.Models.Markers;
import TagProject.example.myapplication.Models.ResponseModel;
import TagProject.example.myapplication.Models.SendModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface JSONPlaceHolderApi {
    @GET("markers")
    Call<ArrayList<Markers>> getAllMarks();

    @POST("markers/upload/")
    Call<ResponseModel> uploadMarker(@Body SendModel sendModel);
}
