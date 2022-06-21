package TagProject.example.Application.Models;

import com.google.gson.annotations.SerializedName;

/** Класс модели для отправки метки на сервер*/
public class SendModel {
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("gps")
    private String gps;
    @SerializedName("marker_type")
    private int marker_type;
    @SerializedName("image")
    private String image;


    /**
     * @param name - имя пользователя (String)
     * @param description - описание метки (String)
     * @param gps - долгота и широта пользователя (String с разделяющей запятой)
     * @param marker_type - тип метки (Integer)
     * @param image - base64 изображения (String)
     * */
    public SendModel(String name, String description, String gps, int marker_type, String image) {
        this.name = name;
        this.description = description;
        this.gps = gps;
        this.marker_type = marker_type;
        this.image = image;
    }

    /**Переопределение метода toString
     * для строкового представления данных */
    @Override
    public String toString() {
        return "SendModel{" +
                "name:\"" + name + '\"' +
                ", description:\"" + description + '\"' +
                ", gps:\"" + gps + '\"' +
                ", marker_type:" + marker_type +
                ", image:\"" + image + '\"' +
                '}';
    }
}
