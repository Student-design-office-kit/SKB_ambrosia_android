package TagProject.example.Application.Models;

import com.google.gson.annotations.SerializedName;

/** Модель ответа от сервера*/
public class ResponseModel {

    @SerializedName("md5")
    private MD5 md5;

    public MD5 getMd5() {
        return md5;
    }

    @SerializedName("msg")
    private String msg;

    public String getMsg() {
        return msg;
    }
}
