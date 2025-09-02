/*
 * (C) Dr. Volker Riediger - Jakarta EE Web Applications
 * University of Koblenz - Faculty 4: Computer Science
 */
package bravo.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Dr. Volker Riediger <riediger@uni-koblenz.de>
 */
public abstract class AbstractDTO implements Serializable {

    private static final long serialVersionUID = 5231885890948093876L;

    private String uuid;
    private int jpaVersion;

    protected AbstractDTO() {
    }

    protected AbstractDTO(String uuid, int jpaVersion) {
        this.uuid = uuid;
        this.jpaVersion = jpaVersion;
    }

    public int getJpaVersion() {
        return jpaVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isNew() {
        return uuid == null;
    }

    @Override
    public int hashCode() {
        if (uuid == null) {
            throw new IllegalStateException("uuid not set");
        }
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.uuid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (uuid == null) {
            throw new IllegalStateException("uuid not set");
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractDTO other = (AbstractDTO) obj;
        return Objects.equals(this.uuid, other.uuid);
    }
}
