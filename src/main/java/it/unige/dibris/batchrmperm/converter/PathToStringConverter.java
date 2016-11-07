package it.unige.dibris.batchrmperm.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.nio.file.Path;
import java.nio.file.Paths;


@Converter(autoApply = true)
public class PathToStringConverter implements AttributeConverter<Path, String> {

    @Override
    public String convertToDatabaseColumn(Path attribute) {
        return attribute.toString();
    }

    @Override
    public Path convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Paths.get(dbData);
    }
}

