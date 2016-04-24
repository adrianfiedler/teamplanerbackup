package de.masalis.teamplanner.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.jboss.as.quickstarts.kitchensink.util.CipherUtil;

@Converter
public class StringCryptoConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String entityString) {
    	return CipherUtil.encrypt(entityString);
    }

    @Override
    public String convertToEntityAttribute(String databaseString) {
      return CipherUtil.decrypt(databaseString);
    }
}