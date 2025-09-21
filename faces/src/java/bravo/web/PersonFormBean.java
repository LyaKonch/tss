package bravo.web;

import bravo.dto.PersonDTO;
import bravo.dto.UserDTO;
import bravo.enums.GroupName;
import bravo.enums.FederalState;
import bravo.logic.PersonLogic;
import bravo.logic.UserLogic;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class PersonFormBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(PersonFormBean.class.getName());

    @Inject
    private PersonLogic personLogic;

    @Inject
    private UserLogic userLogic;

    private List<PersonDTO> allPersons = new ArrayList<>();
    private List<UserDTO> allUsers = new ArrayList<>();

    private PersonDTO selectedPerson = new PersonDTO();
    private String password;
    private List<GroupName> selectedGroups = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadData();
        if (selectedPerson == null) selectedPerson = new PersonDTO();
        if (selectedGroups == null) selectedGroups = new ArrayList<>();
        LOG.fine("PersonFormBean initialized");
    }

    private void loadData() {
        try {
            allPersons = personLogic.findAll();
        } catch (Exception e) {
            LOG.warning("Failed to load persons: " + e.getMessage());
            allPersons = new ArrayList<>();
        }
        try {
            allUsers = userLogic.findAll();
        } catch (Exception e) {
            LOG.warning("Failed to load users: " + e.getMessage());
            allUsers = new ArrayList<>();
        }
    }


    public List<String> getGroupsForPerson(PersonDTO person) {
        if (allUsers == null || person == null || person.getEmailAddress() == null) {
            return Collections.emptyList();
        }
        return allUsers.stream()
                .filter(user -> {

                    String uEmail = user.getEmail();
                    if (uEmail == null) uEmail = user.getEmail();
                    return person.getEmailAddress().equals(uEmail);
                })
                .findFirst()
                .map(UserDTO::getGroupName)
                .orElse(Collections.emptyList());
    }

    public void prepareNewPerson() {
        this.selectedPerson = new PersonDTO();
        this.password = null;
        this.selectedGroups = new ArrayList<>();
        LOG.fine("prepareNewPerson: new PersonDTO initialized");
    }

    public void prepareEditPerson(PersonDTO person) {
        if (person == null) {
            LOG.warning("prepareEditPerson called with null");
            return;
        }

        this.selectedPerson = person;
        this.password = null;
        this.selectedGroups = new ArrayList<>();

        if (allUsers != null) {
            allUsers.stream()
                    .filter(u -> {
                        String uEmail = u.getEmail();
                        if (uEmail == null) uEmail = u.getEmail();
                        return uEmail != null && uEmail.equals(person.getEmailAddress());
                    })
                    .findFirst()
                    .ifPresent(u -> {
                        List<String> g = u.getGroupName();
                        if (g != null) {
                            try {
                                this.selectedGroups = g.stream().map(GroupName::valueOf).collect(Collectors.toList());
                            } catch (Exception ex) {
                                LOG.warning("Failed to map group names to enum: " + ex.getMessage());
                                this.selectedGroups = new ArrayList<>();
                            }
                        }
                    });
        }
        LOG.fine("prepareEditPerson: selectedPerson=" + selectedPerson + ", selectedGroups=" + selectedGroups);
    }


    public void savePerson() {
        try {
            if (selectedPerson == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No person data to save.");
                return;
            }

            boolean isNew = selectedPerson.getId() == null;
            List<String> groupNamesAsString = selectedGroups == null ? List.of()
                    : selectedGroups.stream().map(Enum::name).collect(Collectors.toList());

            if (isNew) {
                PersonDTO createdPerson = personLogic.create(selectedPerson);
                LOG.fine("Created person: " + createdPerson);

                if (password != null && !password.isBlank()) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setEmail(createdPerson.getEmailAddress());
                    userDTO.setPassword(password);
                    userDTO.setGroupName(groupNamesAsString);
                    userLogic.createUser(userDTO);
                    LOG.fine("Created user for person: " + userDTO.getEmail());
                }
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Person and user account created.");
            } else {
                personLogic.update(selectedPerson);
                LOG.fine("Updated person: " + selectedPerson);

                UserDTO userDTO = userLogic.findByEmail(selectedPerson.getEmailAddress());
                if (userDTO != null) {
                    if (password != null && !password.isBlank()) {
                        userDTO.setPassword(password);
                    }
                    userDTO.setGroupName(groupNamesAsString);
                    userLogic.updateUser(userDTO);
                    LOG.fine("Updated user for person: " + userDTO.getEmail());
                } else if (!groupNamesAsString.isEmpty() || (password != null && !password.isBlank())) {
                    UserDTO newUser = new UserDTO();
                    newUser.setEmail(selectedPerson.getEmailAddress());
                    newUser.setPassword(password == null ? "" : password);
                    newUser.setGroupName(groupNamesAsString);
                    userLogic.createUser(newUser);
                    LOG.fine("Created user for existing person: " + newUser.getEmail());
                }
                addMessage(FacesMessage.SEVERITY_INFO, "Success", "Person and user account updated.");
            }

            loadData();

        } catch (Exception e) {
            LOG.severe("savePerson failed: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Operation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- Getters / setters ---
    public List<PersonDTO> getAllPersons() { return allPersons == null ? List.of() : allPersons; }
    public PersonDTO getSelectedPerson() { return selectedPerson; }
    public void setSelectedPerson(PersonDTO selectedPerson) { this.selectedPerson = selectedPerson; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<GroupName> getSelectedGroups() { return selectedGroups == null ? new ArrayList<>() : selectedGroups; }
    public void setSelectedGroups(List<GroupName> selectedGroups) { this.selectedGroups = selectedGroups; }

    public FederalState[] getFederalStates() { return FederalState.values(); }
    public GroupName[] getGroupNames() { return GroupName.values(); }
}
