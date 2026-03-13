package com.example.HAllTicket.converter;

import com.example.HAllTicket.dto.SubjectDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class SubjectListConverter implements AttributeConverter<List<SubjectDTO>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<SubjectDTO> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return "[]";
        }
        try {
            return mapper.writeValueAsString(subjects);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @Override
    public List<SubjectDTO> convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<SubjectDTO>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
