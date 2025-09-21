package bravo.web;

import bravo.dto.ContractDTO;
import bravo.dto.PersonDTO;
import bravo.enums.TimesheetFrequency;
import bravo.logic.ContractLogic;
import bravo.logic.PersonLogic;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Backing bean for contract edit view.
 * This bean provides functionality for creating and editing contracts.
 */
@Named
@ViewScoped
public class ContractEditBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ContractEditBean.class.getName());

    private final EnumSet<TimesheetFrequency> frequencies = EnumSet.allOf(TimesheetFrequency.class);

    private final ResourceBundle locale = FacesContext.getCurrentInstance().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");

    @EJB
    private ContractLogic contractLogic;

    @EJB
    private PersonLogic personLogic;

    @Inject
    private ContractListBean contractListBean;

    private ContractDTO contract;

    private PersonDTO employee;

    private PersonDTO supervisor;

    private List<PersonDTO> secretaries = new ArrayList<>();

    private List<PersonDTO> assistants = new ArrayList<>();

    /**
     * Prepares a new contract for creation.
     */
    public void prepareCreate() {
        resetBean();

        // Include required and sensible defaults
        contract = new ContractDTO();
        contract.setFrequency(TimesheetFrequency.WEEKLY);
        contract.setStartDate(LocalDate.now().withDayOfMonth(1));
        contract.setEndDate(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()));
        contract.setHoursPerWeek(40.0);
        contract.setWorkingDaysPerWeek(5);
        contract.setVacationDaysPerYear(20);
    }

    /**
     * Prepare bean to edit a contract.
     *
     * @param dto the contract to edit
     */
    public void prepareEdit(ContractDTO dto) {
        resetBean();

        if (dto == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractNotFound"), null));
            logger.warning("Tried to edit a null contract.");
            return;
        }

        contract = dto;
        employee = personLogic.findByUuid(contract.getEmployeeUuid());
        supervisor = personLogic.findByUuid(contract.getSupervisorUuid());
        for (String uuid : contract.getSecretaryUuids()) secretaries.add(personLogic.findByUuid(uuid));
        for (String uuid : contract.getAssistantUuids()) assistants.add(personLogic.findByUuid(uuid));
    }

    /**
     * Save or update the current contract.
     */
    public void save() {
        logger.info("Saving contract:\n" + contract);
        try {
            if (contract.isNew()) {
                contractLogic.create(contract);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, locale.getString("contractSaveSuccess"), null));
            } else {
                contractLogic.update(contract);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, locale.getString("contractUpdateSuccess"), null));
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, locale.getString("contractSaveError"), e.getMessage()));
        }
        contractListBean.loadContracts();
    }

    /**
     * Resets the bean to its initial state.
     */
    private void resetBean() {
        contract = null;
        employee = null;
        supervisor = null;
        secretaries.clear();
        assistants.clear();
    }


    public String getFullName(PersonDTO person) {
        if (person == null) return "";
        return (person.getFirstName() + " " + person.getLastName()).trim();
    }

    public List<PersonDTO> completeText(String query) {
        List<PersonDTO> persons = personLogic.findAll();

        return persons.stream()
                .filter(name -> (name.getFirstName() + " " + name.getLastName()).toLowerCase().contains(query.toLowerCase()))
                .sorted(Comparator.comparing(PersonDTO::getFirstName).thenComparing(PersonDTO::getLastName))
                .collect(Collectors.toList());
    }

    public ContractDTO getContract() {
        if (contract == null) {
            contract = new ContractDTO();
        }
        return contract;
    }

    public void setContract(ContractDTO contract) {
        this.contract = contract;
    }

    public EnumSet<TimesheetFrequency> getFrequencies() {
        return frequencies;
    }

    public PersonDTO getEmployee() {
        return employee;
    }

    public void setEmployee(PersonDTO employee) {
        if (employee != null) this.contract.setEmployeeUuid(employee.getUuid());
        this.employee = employee;
    }

    public PersonDTO getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(PersonDTO supervisor) {
        if (supervisor != null) this.contract.setSupervisorUuid(supervisor.getUuid());
        this.supervisor = supervisor;
    }

    public List<PersonDTO> getSecretaries() {
        return secretaries;
    }

    public void setSecretaries(List<PersonDTO> secretaries) {
        if (secretaries != null)
            this.contract.setSecretaryUuids(secretaries.stream().map(PersonDTO::getUuid).collect(Collectors.toList()));
        this.secretaries = secretaries;
    }

    public List<PersonDTO> getAssistants() {
        return assistants;
    }

    public void setAssistants(List<PersonDTO> assistants) {
        if (assistants != null)
            this.contract.setAssistantUuids(assistants.stream().map(PersonDTO::getUuid).collect(Collectors.toList()));
        this.assistants = assistants;
    }
}
