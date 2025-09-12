//für sprache
package bravo.web;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Locale;

@Named("userSessionBean")
@SessionScoped
public class UserSessionBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Locale locale = Locale.GERMAN; 
    public Locale getLocale() { 
        return locale; 
    }
    public void changeLang(String code) { 
        this.locale = Locale.forLanguageTag(code); 
    }
}
