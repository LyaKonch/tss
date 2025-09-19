package bravo.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class AbstractDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String uuid;

    private int version;

    protected AbstractDTO() {
    }

    protected AbstractDTO(String uuid, int version) {
        this.uuid = uuid;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isNew() {
        return uuid == null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractDTO other = (AbstractDTO) obj;
        return Objects.equals(this.uuid, other.getUuid());
    }
}
