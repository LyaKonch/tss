package bravo.logic.impl;

import bravo.dao.ContractsAccess;
import bravo.dto.ContractDTO;
import bravo.entities.Contract;
import bravo.enums.ContractStatus;
import bravo.logic.ContractLogic;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ContractLogicBean implements ContractLogic {

    @EJB
    private ContractsAccess contractsAccess;

    @Override
    public ContractDTO createContract(ContractDTO contractDto) {
        // Convert DTO to entity
        bravo.entities.Contract entity = convertToEntity(contractDto);

        // Persist entity
        entity = contractsAccess.createContract(entity);

        // Convert back to DTO and return
        return convertToDto(entity);
    }

    @Override
    public ContractDTO updateContract(ContractDTO contractDto) {
        // Convert DTO to entity
        bravo.entities.Contract entity = convertToEntity(contractDto);

        // Update entity
        entity = contractsAccess.updateContract(entity);

        // Convert back to DTO and return
        return convertToDto(entity);
    }

    @Override
    public ContractDTO getContractByUuid(String uuid) {
        bravo.entities.Contract entity = contractsAccess.findContractByUuid(uuid);
        return entity != null ? convertToDto(entity) : null;
    }

    @Override
    public List<ContractDTO> getAllContracts() {
        List<bravo.entities.Contract> entities = contractsAccess.getAllContracts();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());

        for (bravo.entities.Contract entity : entities) {
            dtos.add(convertToDto(entity));
        }

        return dtos;
    }

    @Override
    public List<ContractDTO> getActiveContracts() {
        List<bravo.entities.Contract> entities = contractsAccess.getActiveContracts();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());

        for (bravo.entities.Contract entity : entities) {
            dtos.add(convertToDto(entity));
        }

        return dtos;
    }

    @Override
    public ContractDTO terminateContract(String uuid, LocalDate terminationDate) {
        bravo.entities.Contract entity = contractsAccess.findContractByUuid(uuid);

        if (entity != null) {
            entity.setStatus(ContractStatus.TERMINATED);
            entity.setTerminationDate(terminationDate);
            entity = contractsAccess.updateContract(entity);
            return convertToDto(entity);
        }

        return null;
    }

    @Override
    public double calculateHoursDue(ContractDTO contract) {
        // Logic for calculating hours due
        return contract.getWorkingDaysPerWeek() * (contract.getHoursPerWeek() / 5.0);
    }

    @Override
    public double calculateVacationHours(ContractDTO contract) {
        // Logic for calculating vacation hours
        return contract.getVacationDaysPerYear() * (contract.getHoursPerWeek() / 5.0);
    }

    /**
     * Converts a Contract DTO to a Contract entity.
     */
    private bravo.entities.Contract convertToEntity(ContractDTO dto) {
        bravo.entities.Contract entity = new bravo.entities.Contract();

        // Set the UUID only if it's not a new contract
        if (dto.getUuid() != null) {
            entity.setUuid(dto.getUuid());
            entity.setVersion(dto.getJpaVersion());
        }

        //noinspection Duplicates --- IntelliJ Duplication Inspection Suppression - No easy way around similar calls
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setFrequency(dto.getFrequency());
        entity.setTerminationDate(dto.getTerminationDate());
        entity.setHoursPerWeek(dto.getHoursPerWeek());
        entity.setWorkingDaysPerWeek(dto.getWorkingDaysPerWeek());
        entity.setVacationDaysPerYear(dto.getVacationDaysPerYear());
        entity.setStatus(dto.getStatus());

        return entity;
    }

    /**
     * Converts a Contract entity to a Contract DTO.
     */
    private ContractDTO convertToDto(Contract entity) {
        ContractDTO dto = new ContractDTO(entity.getUuid(), entity.getVersion());

        //noinspection Duplicates --- IntelliJ Duplication Inspection Suppression - No easy way around similar calls
        dto.setName(entity.getName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setFrequency(entity.getFrequency());
        dto.setTerminationDate(entity.getTerminationDate());
        dto.setHoursPerWeek(entity.getHoursPerWeek());
        dto.setWorkingDaysPerWeek(entity.getWorkingDaysPerWeek());
        dto.setVacationDaysPerYear(entity.getVacationDaysPerYear());
        dto.setStatus(entity.getStatus());

        return dto;
    }
}
