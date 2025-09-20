package bravo.web.converter;

import bravo.dto.PersonDTO;
import bravo.logic.PersonLogic;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@RequestScoped
@FacesConverter(value = "personConverter", managed = true)
public class PersonConverter implements Converter<PersonDTO> {

    @Inject
    private PersonLogic personLogic;

    @Override
    public PersonDTO getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return personLogic.findByUuid(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, PersonDTO person) {
        return (person != null && person.getUuid() != null) ? person.getUuid() : "";
    }
}
