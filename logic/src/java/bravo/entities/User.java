/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bravo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;

@Entity
@NamedQuery(name = "User.findByEmailAddress", query = "SELECT u FROM Person u WHERE u.emailAddress = :emailAddress")
       
/**
 *
 * @author minschloen
 */
public class User extends AbstractEntity implements Serializable{
    private static final long serialVersionUID = 1L;

    @Column(unique = true)
    private String emailAddress;
    @OneToOne
    private Person person;
    public User(){
        this(false);
    }
    public User(boolean newEntity) {
        super(newEntity);
        if (newEntity) {
            person = new Person();
        }
    }
    public String getEmailAddress(){
        return person.getEmailAddress();
    }
    public void setEmailAddress(String emailAddress){
        this.emailAddress=emailAddress;
    }
}
