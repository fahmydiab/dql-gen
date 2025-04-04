package com.fahmydiab.dqlgen;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class EmbeddingVectorConverter implements AttributeConverter<List<Float>, float[]> {

    @Override
    public float[] convertToDatabaseColumn(List<Float> attribute) {
        if (attribute == null) {
            return null;
        }
        float[] result = new float[attribute.size()];
        for (int i = 0; i < attribute.size(); i++) {
            result[i] = attribute.get(i);
        }
        return result;
    }

    @Override
    public List<Float> convertToEntityAttribute(float[] dbData) {
        if (dbData == null) {
            return null;
        }
        List<Float> result = new ArrayList<>(dbData.length);
        for (float value : dbData) {
            result.add(value);
        }
        return result;
    }
}
