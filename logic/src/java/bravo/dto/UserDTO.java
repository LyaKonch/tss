/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bravo.dto;

import java.util.UUID;

/**
 *
 * @author minschloen
 */
public class UserDTO extends AbstractDTO{
    
    private static final long serialVersionUID = 1L;

    private final String emailAddress;
    
    
    public UserDTO(Long id, String uuid, int version, String emailAddress) {
        super(id, uuid, version);
        this.emailAddress = emailAddress;
    }
    public String getEmailAddress(){
        return emailAddress;
    }
}
