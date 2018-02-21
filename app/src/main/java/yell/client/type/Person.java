package yell.client.type;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by abdulkerim on 28.05.2016.
 */
public class Person implements Serializable {
    public String name;
    public String description;
    public String imageId;
    public String signupDate;

    public Person(String name, String description, String imageId, String signupDate) {
        this.name = name;
        this.description = description;
        this.imageId = imageId;
        this.signupDate = signupDate;
    }

    public Person() {}
}
