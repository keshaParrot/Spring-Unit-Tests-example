package ivan.denysiuk.learntest.services;

import ivan.denysiuk.learntest.repositories.EmployeeRepository;
import ivan.denysiuk.learntest.domains.dtos.EmployeeDto;
import ivan.denysiuk.learntest.domains.entities.Employee;
import ivan.denysiuk.learntest.domains.entities.Shift;
import ivan.denysiuk.learntest.domains.mappers.EmployeeMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @InjectMocks
    EmployeeServiceImpl employeeService;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    EmployeeMapper employeeMapper;
    Employee employee1;
    EmployeeDto employee2;
    @BeforeEach
    void setUp() {
        Stream<Shift> randomShifts = Stream.generate(() -> {
            String startTime = randomTime();
            String endTime = randomTime();
            return Shift.builder()
                    .station("Station " + (new Random().nextInt(10) + 1))
                    .date(LocalDate.of(2024,5,5))
                    .startTime(startTime)
                    .endTime(endTime)
                    .actualStartTime("00:01")
                    .actualEndTime("23:59")
                    .employee(null)
                    .build();
        }).limit(5);

        employee1 = Employee.builder()
                .firstName("Adam")
                .lastName("Kowalski")
                .PESEL("03947283728")
                .rate(23.5)
                .workedShift(randomShifts.collect(Collectors.toUnmodifiableSet()))
                .build();

        employee2 = EmployeeDto.builder()
                .firstName("Iga")
                .lastName("Nowak")
                .rate(23.5)
                .build();
    }

    String randomTime() {
        int hour = new Random().nextInt(24);
        int minute = new Random().nextInt(60);
        return String.format("%02d:%02d", hour, minute);
    }

    @Test
    void getAllEmployees_whenEmployeesExistOnDB_notNull() {
        Page<Employee> mockedPage = new PageImpl<>(List.of(employee1, new Employee()));

        Mockito.when(employeeRepository.findAll(PageRequest.of(0, 2))).thenReturn(mockedPage);

        Page<Employee> pagedEmployee = employeeService.getAllEmployees(0,2);
        List<Employee> allEmployee = pagedEmployee.getContent();

        assertEquals(2,allEmployee.size());
    }

    @Test
    void getEmployeeById_whenEmployeeExistOnDB_notNull() {
        Long employeeId = 1L;

        when(employeeRepository.getEmployeeById(employeeId)).thenReturn(employee1);

        Employee expectedEmployee = employeeService.getEmployeeById(employeeId);
        Assertions.assertNotNull(expectedEmployee, "Returned employee should not be null");
    }

    /*@Test
    void getEmployeeByPESEL_whenEmployeeExistOnDB_notNull() {
        when(employeeRepository.getEmployeeByPESEL("03947283728")).thenReturn(employee1);

        Employee expectedEmployee = employeeService.getEmployeeByPESEL("03947283728");
        Assertions.assertNotNull(expectedEmployee, "Returned employee should not be null");
    }*/

    @Test
    void addEmployeeToDatabase_whenEmployeeNotNull_notNull() {
        Employee convertedEmployee = getEmployeeFromDto(employee2);

        employeeService.saveEmployee(employee2);

        verify(employeeRepository, times(1)).save(convertedEmployee);
    }

    @Test
    void updateEmployeeOnDatabase() {
        Employee convertedEmployee = getEmployeeFromDto(employee2);
        convertedEmployee.setId(0L);

        employeeService.updateEmployee(anyLong(),employee2);

        verify(employeeRepository, times(1)).save(convertedEmployee);
    }
    @Test
    void patchEmployee(){
        EmployeeDto employeeToUpdate = employee2;
        when(employeeRepository.getEmployeeById(anyLong())).thenReturn(employee1);

        employeeService.patchEmployee(anyLong(),employeeToUpdate);

        verify(employeeRepository, times(1)).save(employee1);
    }

    @Test
    void deleteEmployeeFromDatabase_whenEmployeeExistOnDB_notNull() {
        when(employeeRepository.existsById(anyLong())).thenReturn(true);

        employeeService.deleteEmployee(anyLong());

        verify(employeeRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void countAllEmployees_whenEmployeesExistOnDB_notNull() {
        long expectedCount = 5;
        when(employeeRepository.count()).thenReturn(expectedCount);

        long actualCount = employeeService.countAllEmployees();

        assertEquals(expectedCount, actualCount, "The count of employees should match the expected count");

    }

    @Test
    void getMonthSalaryEmployee_whenEmployeesExistOnDB_notNull() {
        Long employeeId = 1L;

        when(employeeRepository.getEmployeeById(employeeId)).thenReturn(employee1);

        double monthSalary = employeeService.getMonthSalary(employeeId,5);

        Assertions.assertTrue(monthSalary > 0, "The month salary should be greater than 0");
    }
    @Test
    void getMonthTax_whenEmployeesExistOnDB_notNull() {
        Long employeeId = 1L;

        when(employeeRepository.getEmployeeById(employeeId)).thenReturn(employee1);

        Map<String, Double> mapOfTax = employeeService.getMonthTax(employeeId,5);

        assertNotNull(mapOfTax,"The month salary should be not null");
    }
    @Test
    void getMonthRevenue_whenEmployeesExistOnDB_notNull() {
        Long employeeId = 1L;

        when(employeeRepository.getEmployeeById(employeeId)).thenReturn(employee1);

        double monthRevenue = employeeService.getMonthRevenue(employeeId,5);

        Assertions.assertTrue(monthRevenue > 0.0, "The month salary should be greater than 0");
    }
    Employee getEmployeeFromDto(EmployeeDto employee){
        return EmployeeMapper.INSTANCE.dtoToEmployee(employee);
    }
}