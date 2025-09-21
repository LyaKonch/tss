package bravo.mapper;

/**
 * Generic abstract mapper base class for converting between entities and DTOs.
 * <p>
 * Concrete mappers should extend this class and implement the conversion logic
 * between the entity type {@code Entity} and the DTO type {@code Dto}.
 *
 * @param <Entity> type of the entity
 * @param <Dto>    type of the data transfer object (DTO)
 */
public abstract class AbstractMapper<Entity, Dto> {

    /**
     * Converts a DTO to an entity.
     *
     * @param dto the data transfer object
     * @return the corresponding entity
     */
    public abstract Entity toEntity(Dto dto);

    /**
     * Converts an entity to a DTO.
     *
     * @param entity the entity object
     * @return the corresponding DTO
     */
    public abstract Dto toDto(Entity entity);
}