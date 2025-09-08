package bravo.logic.impl;

import bravo.dao.ContractDAO;
import bravo.dto.ContractDTO;
import bravo.entities.Contract;
import bravo.enums.ContractStatus;
import bravo.logic.ContractLogic;
import bravo.mapper.ContractMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ContractLogicBean implements ContractLogic {

    @EJB
    private ContractDAO contractDAO;

    @Inject
    private ContractMapper ContractMapper;

    @Override
    public ContractDTO createContract(ContractDTO contractDto) {
        // Convert DTO to entity
        Contract entity = ContractMapper.toEntity(contractDto);

        // Persist entity
        contractDAO.save(entity);

        // Convert back to DTO and return
        return ContractMapper.toDto(entity);
    }

    @Override
    public ContractDTO updateContract(ContractDTO contractDto) {
        // Convert DTO to entity
        Contract entity = ContractMapper.toEntity(contractDto);

        // Update entity
        entity = contractDAO.updateContract(entity);

        // Convert back to DTO and return
        return ContractMapper.toDto(entity);
    }

    @Override
    public ContractDTO getContractByUuid(String uuid) {
        Contract entity = contractDAO.findContractByUuid(uuid);
        return entity != null ? ContractMapper.toDto(entity) : null;
    }

    @Override
    public List<ContractDTO> getAllContracts() {
        List<Contract> entities = contractDAO.getAllContracts();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());

        for (Contract entity : entities) {
            dtos.add(ContractMapper.toDto(entity));
        }

        return dtos;
    }

    @Override
    public List<ContractDTO> getActiveContracts() {
        List<Contract> entities = contractDAO.getActiveContracts();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());

        for (Contract entity : entities) {
            dtos.add(ContractMapper.toDto(entity));
        }

        return dtos;
    }

    @Override
    public ContractDTO terminateContract(String uuid, LocalDate terminationDate) {
        Contract entity = contractDAO.findContractByUuid(uuid);

        if (entity != null) {
            entity.setStatus(ContractStatus.TERMINATED);
            entity.setTerminationDate(terminationDate);
            entity = contractDAO.updateContract(entity);
            return ContractMapper.toDto(entity);
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


}
