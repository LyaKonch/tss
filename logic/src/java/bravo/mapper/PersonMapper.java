package bravo.mapper;

import bravo.dto.PersonDTO;
import bravo.entities.Person;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class PersonMapper extends AbstractMapper<Person, PersonDTO> {

    @Override
    public Person toEntity(PersonDTO dto) {
        Person entity = new Person();

        if (dto.getId() != null) entity.setId(dto.getId());
        if (dto.getUuid() != null) entity.setUuid(UUID.fromString(dto.getUuid()));

        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setEmailAddress(dto.getEmailAddress());
        entity.setConsent(dto.isConsent());

        return entity;
    }

    @Override
    public PersonDTO toDto(Person entity) {
        PersonDTO dto = new PersonDTO();

        dto.setId(entity.getId());
        dto.setUuid(entity.getUuid().toString());

        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setEmailAddress(entity.getEmailAddress());
        dto.setConsent(entity.isConsent());

        return dto;
    }
}
