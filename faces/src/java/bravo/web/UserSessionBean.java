package bravo.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

@Named
@SessionScoped
public class UserSessionBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Locale locale = Locale.GERMAN;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setLocale(String code) {
        this.locale = Locale.forLanguageTag(code);
    }
}
