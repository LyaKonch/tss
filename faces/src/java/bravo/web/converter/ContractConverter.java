package bravo.web.converter;

import bravo.dto.ContractDTO;
import bravo.logic.ContractLogic;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@RequestScoped
@FacesConverter(value = "contractConverter", managed = true)
public class ContractConverter implements Converter<ContractDTO> {

    @Inject
    private ContractLogic contractLogic;

    @Override
    public ContractDTO getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return contractLogic.findByUuid(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ContractDTO contract) {
        return (contract != null && contract.getUuid() != null) ? contract.getUuid() : "";
    }
}
