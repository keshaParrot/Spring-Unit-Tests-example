package ivan.denysiuk.learntest.Services;

import ivan.denysiuk.learntest.Repository.EmployeeRepository;
import ivan.denysiuk.learntest.Repository.ShiftRepository;
import ivan.denysiuk.learntest.domain.entity.Employee;
import ivan.denysiuk.learntest.domain.entity.Shift;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceImplTest {

    @InjectMocks
    ShiftServiceImpl shiftService;
    @Mock
    ShiftRepository shiftRepository;
    @Mock
    EmployeeRepository employeeRepository;
    Shift shift;
    Employee employee;
    Long shiftId = 1L;
    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("Adam")
                .lastName("Kowalski")
                .PESEL("03947283728")
                .rate(23.5)
                .build();

        shift = Shift.builder()
                .id(shiftId)
                .actualStartTime("12:30")
                .actualEndTime("22:30")
                .employee(employee)
                .build();
    }

    @Test
    void getShiftById_whenShiftExistOnDB_shift() {
        when(shiftRepository.getShiftById(shiftId)).thenReturn(shift);

        Shift expectedShift = shiftService.getShiftById(shiftId);
        Assertions.assertNotNull(expectedShift, "Returned employee should not be null");
    }

    @Test
    void getAllShiftByEmployee_whenShiftExistOnDB_true() {

        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(shiftRepository.getShiftsByEmployeeId(1L)).thenReturn(Arrays.asList(new Shift(), new Shift()));

        List<Shift> expectedShift = shiftService.getAllShiftByEmployee(1L);
        assertEquals(2,expectedShift.size());
    }
    @Test
    void getAllShiftByEmployee_whenShiftNotExistOnDB_false() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(shiftRepository.getShiftsByEmployeeId(1L)).thenReturn(Collections.emptyList());

        List<Shift> expectedShift = shiftService.getAllShiftByEmployee(1L);
        assertTrue(expectedShift.isEmpty());

    }
    @Test
    @Disabled
    void getAllShiftByEmployeeFromToDate_whenShiftExistOnDB_shift() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(shiftRepository.getShiftsByEmployeeId(1L)).thenReturn(Arrays.asList(
                Shift.builder()
                        .date(LocalDate.of(2024,1,23))
                        .build(),
                Shift.builder()
                        .date(LocalDate.of(2024,4,23))
                        .build()));

        List<Shift> expectedShift = shiftService.getAllShiftByEmployeeFromToDate(1L, YearMonth.of(2024,2),YearMonth.of(2024,4));
        assertFalse(expectedShift.isEmpty());
    }

    @Test
    void addShiftToDatabase_whenShiftNotNull_shift() {
        shiftService.addShiftToDatabase(shift);

        verify(shiftRepository, times(1)).save(shift);
    }

    @Test
    void deleteShiftFromDatabase_whenShiftExistOnDB_true() {
        when(shiftRepository.existsById(1L)).thenReturn(true);

        shiftService.deleteShiftFromDatabase(1L);

        verify(shiftRepository, times(1)).deleteById(1L);
    }

    @Test
    void changeEmployee_whenShiftExistOnDBEmployeeNotNull_0() {
        Shift newShift = Shift.builder()
                .id(2L)
                .build();

        when(shiftRepository.getShiftById(2L)).thenReturn(newShift);
        when(employeeRepository.getEmployeeById(1L)).thenReturn(employee);

        int errorCode = shiftService.changeEmployee(2L,1L);

        when(shiftRepository.getShiftById(2L)).thenReturn(newShift);


        Shift updatedShift = shiftService.getShiftById(2L);

        assertEquals(1L, updatedShift.getEmployee().getId());
        assertEquals(0,errorCode);
    }
    @Test
    void changeEmployee_whenShiftNotExistOnDBEmployeeNotNull_1() {
        when(employeeRepository.getEmployeeById(1L)).thenReturn(employee);
        when(shiftRepository.getShiftById(2L)).thenReturn(null);

        int errorCode = shiftService.changeEmployee(2L,1L);

        assertEquals(1,errorCode);
    }
    @Test
    void changeWorkedTime_whenShiftExistOnDBTimeIsValid_0() {
        when(shiftRepository.getShiftById(1L)).thenReturn(shift);

        int errorCode = shiftService.changeWorkedTime(1L,"12:00","16:00");
        assertEquals(0,errorCode);
    }
    @Test
    void changeWorkedTime_whenShiftExistOnDBTimeIsNotValid_1() {

        int errorCode = shiftService.changeWorkedTime(1L,"122:00","160:00");
        assertEquals(1,errorCode);
    }
    @Test
    void changeActualWorkedTime_whenShiftExistOnDBTimeIsValid_0(){
        Long shiftId = 1L;
        String startTime = "09:00";
        String endTime = "17:00";

        when(shiftRepository.getShiftById(shiftId)).thenReturn(shift);

        int result = shiftService.changeActualWorkTime(shiftId, startTime, endTime);
        Assertions.assertEquals(0, result);
        verify(shiftRepository, times(1)).save(shift);
    }

    @Test
    void countAllShift() {
        long expectedCount = 5;
        when(shiftRepository.count()).thenReturn(expectedCount);

        long actualCount = shiftService.countAllShift();

        assertEquals(expectedCount, actualCount, "The count of employees should match the expected count");
    }
}