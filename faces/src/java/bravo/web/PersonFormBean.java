package bravo.web;

import bravo.dto.PersonDTO;
import bravo.dto.UserDTO;
import bravo.enums.GroupName;
import bravo.enums.FederalState;
import bravo.logic.PersonLogic;
import bravo.logic.UserLogic;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.logging.Logger;

@Named
@RequestScoped
public class PersonFormBean {

    private static final Logger logger = Logger.getLogger(PersonFormBean.class.getName());

    @Inject
    private PersonLogic personLogic;

    @Inject
    private UserLogic userLogic;

    private PersonDTO personDTO;
    private String password;
    private GroupName[] groupNames;
    private FederalState[] federalStates;

    private GroupName selectedGroup;

    @PostConstruct
    public void init() {
        personDTO = new PersonDTO();
        federalStates = FederalState.values();
        groupNames = GroupName.values();
    }

    public String savePerson() {
        try {
            personDTO.setState(federalStates[0]);
            PersonDTO createdPerson = personLogic.create(personDTO);

            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(createdPerson.getEmailAddress());
            userDTO.setPassword(password);
            userDTO.setGroupName(selectedGroup.toString());

            userLogic.createUser(userDTO);

            logger.info("New person and user account created successfully for " + createdPerson.getEmailAddress());

            return "dashboard";
        } catch (Exception e) {
            logger.severe("Error creating person or user account: " + e.getMessage());
            return null;
        }
    }

    // Getters and Setters
    public PersonDTO getPersonDTO() { return personDTO; }
    public void setPersonDTO(PersonDTO personDTO) { this.personDTO = personDTO; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public GroupName getSelectedGroup() { return selectedGroup; }
    public void setSelectedGroup(GroupName selectedGroup) { this.selectedGroup = selectedGroup; }

    public GroupName[] getGroupNames() { return groupNames; }

    public FederalState[] getFederalStates() { return federalStates; }

}
