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
    public ContractDTO create(ContractDTO contractDto) {
        Contract entity = ContractMapper.toEntity(contractDto);
        contractDAO.save(entity);
        return ContractMapper.toDto(entity);
    }

    @Override
    public ContractDTO update(ContractDTO contractDto) {
        Contract entity = ContractMapper.toEntity(contractDto);
        contractDAO.save(entity);
        return ContractMapper.toDto(entity);
    }

    @Override
    public ContractDTO findByUuid(String uuid) {
        Contract entity = contractDAO.findByUuid(uuid);
        return entity != null ? ContractMapper.toDto(entity) : null;
    }

    @Override
    public List<ContractDTO> findAll() {
        List<Contract> entities = contractDAO.findAll();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());
        for (Contract entity : entities) dtos.add(ContractMapper.toDto(entity));
        return dtos;
    }

    @Override
    public List<ContractDTO> getActive() {
        List<Contract> entities = contractDAO.getActiveContracts();
        List<ContractDTO> dtos = new ArrayList<>(entities.size());

        for (Contract entity : entities) {
            dtos.add(ContractMapper.toDto(entity));
        }

        return dtos;
    }

    @Override
    public ContractDTO terminate(String uuid, LocalDate terminationDate) {
        Contract entity = contractDAO.findByUuid(uuid);

        if (entity != null) {
            if (terminationDate.isBefore(LocalDate.now())) entity.setStatus(ContractStatus.TERMINATED);
            entity.setTerminationDate(terminationDate);
            contractDAO.save(entity);
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
