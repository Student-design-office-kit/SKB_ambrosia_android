package TagProject.example.Application.Models;

import com.google.gson.annotations.SerializedName;

/** Модель ответа от сервера*/
public class ResponseModel {
    @SerializedName("msg")
    private String msg;

    public String getMsg() {
        return msg;
    }
}
