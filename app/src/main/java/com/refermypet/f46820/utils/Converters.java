package com.refermypet.f46820.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.refermypet.f46820.model.Pet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {

    @TypeConverter
    public static String fromPetList(List<Pet> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<Pet> toPetList(String value) {
        if (value == null) return null;
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Pet>>() {}.getType();
        return gson.fromJson(value, listType);
    }
}