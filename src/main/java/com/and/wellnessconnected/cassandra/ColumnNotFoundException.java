package com.and.wellnessconnected.cassandra;

/**
 * Created by charliefederspiel on 2/25/14.
 */
public class ColumnNotFoundException extends Exception {
    public ColumnNotFoundException(String desc) {
        super(desc);
    }
}
