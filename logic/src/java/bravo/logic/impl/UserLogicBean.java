package bravo.logic.impl;

import bravo.dao.UserDAO;
import bravo.dto.UserDTO;
import bravo.entities.User;
import bravo.logic.UserLogic;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Stateless;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import java.security.Principal;



@Stateless
public class UserLogicBean implements UserLogic {
@EJB
private UserDAO ud;
@Resource
private EJBContext ejbContext;

private User caller;
@AroundInvoke
    private Object getCaller(InvocationContext ctx) throws Exception {
        Principal p = ejbContext.getCallerPrincipal();
        if (p != null) {
            caller = ud.getUser(p.getName());
        }
        return ctx.proceed();
    }
    @Override
    public UserDTO getCurrentUserDTO() {
        return createDTO(caller);
    }
    private UserDTO createDTO(User ue) {
        return new UserDTO(ue.getId(), ue.getUuid(), ue.getVersion(), ue.getEmailAddress());
    }
    
}
