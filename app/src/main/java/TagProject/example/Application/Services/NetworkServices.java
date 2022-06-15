package TagProject.example.Application.Services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Класс для отправки запросов на сервер */
public class NetworkServices {
    private static NetworkServices mInstance;
    private static final String BASE_URL =
            "http://tagproject-api.sfedu.ru/api/v1/map/"; //адрес сервера
    private Retrofit mRetrofit; // Объект для отправки запросов

    /** Конструктор класса */
    private NetworkServices() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }


    /** Метод для получения instance */
    public static NetworkServices getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkServices();
        }
        return mInstance;
    }

    /** Метод для обращения к вспомогательному интерфейсу */
    public JSONPlaceHolderApi getJSONApi() {
        return mRetrofit.create(JSONPlaceHolderApi.class);
    }
}
