package TagProject.example.Application.Services;
import TagProject.example.Application.Models.Markers;
import TagProject.example.Application.Models.ResponseModel;
import TagProject.example.Application.Models.SendModel;

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
