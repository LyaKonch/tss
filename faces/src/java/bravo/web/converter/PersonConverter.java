package bravo.web.converter;

import bravo.dto.PersonDTO;
import bravo.web.ContractFormBean;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.util.List;

@FacesConverter(value = "personConverter", managed = true)
public class PersonConverter implements Converter<PersonDTO> {

    @Override
    public PersonDTO getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        ContractFormBean bean = context.getApplication()
                .evaluateExpressionGet(context, "#{contractFormBean}", ContractFormBean.class);

        List<PersonDTO> candidates = bean.completeText(""); // get all persons
        return candidates.stream()
                .filter(p -> value.equals(p.getUuid()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, PersonDTO person) {
        return (person != null && person.getUuid() != null) ? person.getUuid() : "";
    }
}
