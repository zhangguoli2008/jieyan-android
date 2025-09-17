package com.quitbuddy.data.converter;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringListConverter {
    private static final String DELIMITER = ",";

    @TypeConverter
    public static String fromList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(DELIMITER, values);
    }

    @TypeConverter
    public static List<String> toList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(DELIMITER)));
    }
}
