package kroryi.bus2.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kroryi.bus2.entity.user.Role;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        return role == null ? null : role.name(); // enum -> String
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return Role.valueOf(dbData.toUpperCase()); // ← 이게 핵심!
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown role: " + dbData);
        }
    }
}

