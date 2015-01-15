package network.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by umeshchandrayadav on 15/01/15.
 */
public class MediaDTO implements Serializable{

    @SerializedName("m") private String image;


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
