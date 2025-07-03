package bravo.dto;

import bravo.enums.ContractStatus;
import bravo.enums.TimesheetFrequency;

import java.io.Serial;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for Contract information.
 * This class extends AbstractDTO to inherit common DTO functionality.
 */
public class ContractDto extends AbstractDto {

    @Serial
    private static final long serialVersionUID = 1L;

    private ContractStatus status;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private TimesheetFrequency frequency;
    private LocalDate terminationDate;
    private double hoursPerWeek;
    private int workingDaysPerWeek;
    private int vacationDaysPerYear;

    /**
     * Default constructor.
     */
    public ContractDto() {
        super();
    }

    /**
     * Constructor with UUID and JPA version.
     *
     * @param uuid    the unique identifier
     * @param version the JPA version
     */
    public ContractDto(String uuid, int version) {
        super(uuid, version);
    }


    /**
     * Gets the status of the contract.
     *
     * @return the contract status
     */
    public ContractStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the contract.
     *
     * @param status the contract status
     */
    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    /**
     * Gets the name of the contract.
     *
     * @return the contract name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the contract.
     *
     * @param name the contract name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the start date of the contract.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the contract.
     *
     * @param startDate the start date
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the end date of the contract.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the contract.
     *
     * @param endDate the end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets the timesheet submission frequency.
     *
     * @return the timesheet frequency
     */
    public TimesheetFrequency getFrequency() {
        return frequency;
    }

    /**
     * Sets the timesheet submission frequency.
     *
     * @param frequency the timesheet frequency
     */
    public void setFrequency(TimesheetFrequency frequency) {
        this.frequency = frequency;
    }

    /**
     * Gets the termination date of the contract, if applicable.
     *
     * @return the termination date
     */
    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    /**
     * Sets the termination date of the contract.
     *
     * @param terminationDate the termination date
     */
    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    /**
     * Gets the hours per week for this contract.
     *
     * @return the hours per week
     */
    public double getHoursPerWeek() {
        return hoursPerWeek;
    }

    /**
     * Sets the hours per week for this contract.
     *
     * @param hoursPerWeek the hours per week
     */
    public void setHoursPerWeek(double hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    /**
     * Gets the number of working days per week.
     *
     * @return the working days per week
     */
    public int getWorkingDaysPerWeek() {
        return workingDaysPerWeek;
    }

    /**
     * Sets the number of working days per week.
     *
     * @param workingDaysPerWeek the working days per week
     */
    public void setWorkingDaysPerWeek(int workingDaysPerWeek) {
        this.workingDaysPerWeek = workingDaysPerWeek;
    }

    /**
     * Gets the number of vacation days per year.
     *
     * @return the vacation days per year
     */
    public int getVacationDaysPerYear() {
        return vacationDaysPerYear;
    }

    /**
     * Sets the number of vacation days per year.
     *
     * @param vacationDaysPerYear the vacation days per year
     */
    public void setVacationDaysPerYear(int vacationDaysPerYear) {
        this.vacationDaysPerYear = vacationDaysPerYear;
    }

    /**
     * Calculates the hours due based on working days per week and hours per week.
     * This is a derived property (not stored directly).
     *
     * @return the calculated hours due
     */
    public double getHoursDue() {
        // TODO: Use calculation as described in requirements
        return workingDaysPerWeek * (hoursPerWeek / 5.0);
    }

    /**
     * Calculates the vacation hours based on vacation days per year and hours per week.
     * This is a derived property (not stored directly).
     *
     * @return the calculated vacation hours
     */
    public double getVacationHours() {
        return vacationDaysPerYear * (hoursPerWeek / 5.0);
    }
}
