package bravo.mapper;

public abstract class AbstractMapper<Entity, Dto> {
    public abstract Entity toEntity(Dto dto);

    public abstract Dto toDto(Entity entity);
}
