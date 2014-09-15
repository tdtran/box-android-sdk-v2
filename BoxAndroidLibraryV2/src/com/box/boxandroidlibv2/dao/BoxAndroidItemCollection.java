package com.box.boxandroidlibv2.dao;

import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

import com.box.boxjavalibv2.dao.BoxItemCollection;

/**
 * Data class for Collaboration.
 */
public class BoxAndroidItemCollection extends BoxItemCollection implements Parcelable {

    public BoxAndroidItemCollection() {
        super();
    }

    private BoxAndroidItemCollection(Parcel in) {
        super(new BoxParcel(in));
    }

    /**
     * Copy constructor, this does deep copy for all the fields.
     * 
     * @param obj
     */
    public BoxAndroidItemCollection(BoxAndroidItemCollection obj) {
        super(obj);
    }

    /**
     * Instantiate the object from a map. Each entry in the map reflects to a field.
     * 
     * @param map
     */
    public BoxAndroidItemCollection(Map<String, Object> map) {
        super(map);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(new BoxParcel(dest), flags);
    }

    public static final Parcelable.Creator<BoxAndroidItemCollection> CREATOR = new Parcelable.Creator<BoxAndroidItemCollection>() {

        @Override
        public BoxAndroidItemCollection createFromParcel(Parcel source) {
            return new BoxAndroidItemCollection(source);
        }

        @Override
        public BoxAndroidItemCollection[] newArray(int size) {
            return new BoxAndroidItemCollection[size];
        }
    };
}
