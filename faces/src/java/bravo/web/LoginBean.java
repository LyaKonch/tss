package bravo.web;

import bravo.logic.UserLogic;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;

@Named
@RequestScoped
public class LoginBean {

    @EJB
    UserLogic userLogicBean;

    private String email;
    private String password;
    private String securityRole;
    private WhoAmI whoAmI;

    // getters / setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public WhoAmI getWhoAmI() {
        return whoAmI;
    }

    public void setWhoAmI(WhoAmI whoAmI) {
        this.whoAmI = whoAmI;
    }

    public String login() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ec.getRequest();
        try {
            req.login(email, password); // delegates to container (jdbcRealm)

            return "/pages/dashboard.xhtml?faces-redirect=true";
        } catch (ServletException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failed", "Invalid credentials"));
            return null; // stay on page
        }
    }

    public String logout() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ec.getRequest();
        try {
            req.logout();
            ec.invalidateSession();
        } catch (ServletException e) {
            // ignore or log
        }
        return "/pages/user/login.xhtml?faces-redirect=true";
    }

    public void whatAmI() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest req = (HttpServletRequest) ec.getRequest();
        Principal p = req.getUserPrincipal();
        if (p == null) {
            whoAmI = null;
            return;
        }
        System.out.println(p.getName());
        whoAmI = new WhoAmI(
                p.getName(),
                req.isUserInRole("ADMIN"),
                req.isUserInRole("USER"),
                req.isUserInRole("STAFF")
        );
    }

    public String getSecurityRole() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return null; // User is not authenticated
        }
        securityRole = "idk what role i am";
        if (request.isUserInRole("ADMIN")) {
            securityRole = "ADMIN";
        }
        if (request.isUserInRole("STAFF")) {
            securityRole = "STAFF";
        }
        if (request.isUserInRole("USER")) {
            securityRole = "USER";
        }

        return securityRole;
    }

    public void setSecurityRole(String securityRole) {
        this.securityRole = securityRole;
    }

    public class WhoAmI {
        public String email;
        public boolean admin;
        public boolean user;
        public boolean stuff;

        public WhoAmI(String email, boolean admin, boolean user, boolean stuff) {
            this.email = email;
            this.admin = admin;
            this.user = user;
            this.stuff = stuff;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean getAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public boolean getUser() {
            return user;
        }

        public void setUser(boolean user) {
            this.user = user;
        }

        public boolean getStuff() {
            return stuff;
        }

    }

}