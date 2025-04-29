package com.example.financery.service.impl;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import com.example.financery.mapper.BillMapper;
import com.example.financery.model.Bill;
import com.example.financery.model.User;
import com.example.financery.repository.BillRepository;
import com.example.financery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BillMapper billMapper;

    @InjectMocks
    private BillServiceImpl billService;

    private Bill bill;
    private User user;
    private BillDtoRequest billDtoRequest;
    private BillDtoResponse billDtoResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setBalance(1000.0);

        bill = new Bill();
        bill.setId(1L);
        bill.setName("Test Bill");
        bill.setBalance(500.0);
        bill.setUser(user);
        bill.setTransactions(new ArrayList<>());

        billDtoRequest = new BillDtoRequest();
        billDtoRequest.setName("Test Bill");
        billDtoRequest.setBalance(500.0);
        billDtoRequest.setUserId(1L);

        billDtoResponse = new BillDtoResponse();
        billDtoResponse.setId(1L);
        billDtoResponse.setName("Test Bill");
        billDtoResponse.setBalance(500.0);
        billDtoResponse.setUserId(1L);
    }

    @Test
    void getAllBills_success() {
        // Проверяет успешное получение всех счетов
        List<Bill> bills = List.of(bill);
        when(billRepository.findAll()).thenReturn(bills);
        when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

        List<BillDtoResponse> result = billService.getAllBills();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(billDtoResponse, result.get(0));
        verify(billRepository).findAll();
        verify(billMapper).toBillDto(bill);
    }

    @Test
    void getAllBills_emptyList() {
        // Проверяет поведение, когда список счетов пуст
        when(billRepository.findAll()).thenReturn(new ArrayList<>());

        List<BillDtoResponse> result = billService.getAllBills();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(billRepository).findAll();
    }

    @Test
    void getBillsByUserId_success() {
        // Проверяет успешное получение счетов по ID пользователя
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.findByUser(1L)).thenReturn(List.of(bill));
        when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

        List<BillDtoResponse> result = billService.getBillsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(billDtoResponse, result.get(0));
        verify(userRepository).findById(1L);
        verify(billRepository).findByUser(1L);
        verify(billMapper).toBillDto(bill);
    }

    @Test
    void getBillsByUserId_userNotFound_throwsNotFoundException() {
        // Проверяет, что выбрасывается исключение, если пользователь не найден
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.getBillsByUserId(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository, never()).findByUser(anyLong());
    }

    @Test
    void getBillById_success() {
        // Проверяет успешное получение счета по ID
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

        BillDtoResponse result = billService.getBillById(1L);

        assertNotNull(result);
        assertEquals(billDtoResponse, result);
        verify(billRepository).findById(1L);
        verify(billMapper).toBillDto(bill);
    }

    @Test
    void getBillById_billNotFound_throwsNotFoundException() {
        // Проверяет, что выбрасывается исключение, если счет не найден
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.getBillById(1L));

        assertEquals("Счет с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
    }

    @Test
    void createBill_success() {
        // Проверяет успешное создание счета
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billMapper.toBill(billDtoRequest)).thenReturn(bill);
        when(billRepository.save(bill)).thenReturn(bill);
        when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

        BillDtoResponse result = billService.createBill(billDtoRequest);

        assertNotNull(result);
        assertEquals(billDtoResponse, result);
        assertEquals(1500.0, user.getBalance()); // Проверяем, что баланс пользователя увеличился
        verify(userRepository).findById(1L);
        verify(billRepository).save(bill);
        verify(billMapper).toBill(billDtoRequest);
        verify(billMapper).toBillDto(bill);
    }

    @Test
    void createBill_negativeBalance_throwsInvalidInputException() {
        // Проверяет, что выбрасывается исключение при отрицательном балансе
        billDtoRequest.setBalance(-100.0);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.createBill(billDtoRequest));

        assertEquals("Баланс счёта не может быть отрицательным", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createBill_emptyName_throwsInvalidInputException() {
        // Проверяет, что выбрасывается исключение при пустом имени счета
        billDtoRequest.setName("");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.createBill(billDtoRequest));

        assertEquals("Имя счёта не может быть пустым", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createBill_userNotFound_throwsNotFoundException() {
        // Проверяет, что выбрасывается исключение, если пользователь не найден
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.createBill(billDtoRequest));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(billRepository, never()).save(any());
    }

    @Test
    void updateBill_success() {
        // Проверяет успешное обновление счета
        billDtoRequest.setBalance(600.0);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(billRepository.save(bill)).thenReturn(bill);
        when(userRepository.save(user)).thenReturn(user);
        when(billMapper.toBillDto(bill)).thenReturn(billDtoResponse);

        BillDtoResponse result = billService.updateBill(1L, billDtoRequest);

        assertNotNull(result);
        assertEquals(billDtoResponse, result);
        assertEquals(1100.0, user.getBalance()); // Проверяем, что баланс пользователя обновлен
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(billRepository).save(bill);
        verify(userRepository).save(user);
        verify(billMapper).toBillDto(bill);
    }

    @Test
    void updateBill_negativeBalance_throwsInvalidInputException() {
        // Проверяет, что выбрасывается исключение при отрицательном балансе
        billDtoRequest.setBalance(-100.0);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Баланс счёта не может быть отрицательным", exception.getMessage());
        verify(billRepository, never()).findById(anyLong());
    }

    @Test
    void updateBill_emptyName_throwsInvalidInputException() {
        // Проверяет, что выбрасывается исключение при пустом имени счета
        billDtoRequest.setName("");

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Имя счёта не может быть пустым", exception.getMessage());
        verify(billRepository, never()).findById(anyLong());
    }

    @Test
    void updateBill_billNotFound_throwsNotFoundException() {
        // Проверяет, что выбрасывается исключение, если счет не найден
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Счет с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
    }

    @Test
    void updateBill_userNotFound_throwsNotFoundException() {
        // Проверяет, что выбрасывается исключение, если пользователь не найден
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.updateBill(1L, billDtoRequest));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void deleteBill_success() {
        // Проверяет успешное удаление счета
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        billService.deleteBill(1L);

        assertEquals(500.0, user.getBalance()); // Проверяем, что баланс пользователя уменьшен
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(billRepository).deleteById(1L);
    }

    @Test
    void deleteBill_billNotFound_throwsNotFoundException() {
        // Проверяет, что выбрасывается исключение, если счет не найден
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.deleteBill(1L));

        assertEquals("Счет с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void deleteBill_userNotFound_throwsNotFoundException() {
        // Проверяет, что выбрасывается исключение, если пользователь не найден
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> billService.deleteBill(1L));

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
        verify(billRepository).findById(1L);
        verify(userRepository).findById(1L);
    }
}