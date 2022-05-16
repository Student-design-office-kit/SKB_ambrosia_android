package TagProject.example.myapplication.Services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkServices {
    private static NetworkServices mInstance;
    private static final String BASE_URL = "http://tagproject-api.sfedu.ru/api/v1/map/";
    private Retrofit mRetrofit;

    private NetworkServices() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static NetworkServices getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkServices();
        }
        return mInstance;
    }

    public JSONPlaceHolderApi getJSONApi() {
        return mRetrofit.create(JSONPlaceHolderApi.class);
    }
}
