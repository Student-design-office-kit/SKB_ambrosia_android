package TagProject.example.Application.Models;

import com.google.gson.annotations.SerializedName;

/**Класс для модели изображения в base64 формате */
public class PhotoBase64 {
    @SerializedName("photo_base64")
    private String photo;

    public PhotoBase64(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

}
