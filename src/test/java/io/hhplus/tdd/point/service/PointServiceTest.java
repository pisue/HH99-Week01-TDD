package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.service.manager.PointHistoryManager;
import io.hhplus.tdd.point.service.manager.PointManager;
import io.hhplus.tdd.point.service.reader.PointHistoryReader;
import io.hhplus.tdd.point.service.reader.PointReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PointServiceTest {
    @InjectMocks
    private PointService pointService;

    @Mock
    private PointReader pointReader;

    @Mock
    private PointHistoryReader pointHistoryReader;

    @Mock
    private PointManager pointManager;


    @Mock
    private PointHistoryManager pointHistoryManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("존재하는 사용자면 포인트를 리턴한다")
    void  사용자_포인트_조회() {
        // Given
        long userId = 1L;
        UserPoint expectedUserPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());
        when(pointReader.read(userId)).thenReturn(expectedUserPoint);

        // When
        UserPoint result = pointService.read(userId);

        // Then
        assertEquals(expectedUserPoint, result);
        verify(pointReader, times(1)).read(userId);
    }


    @Test
    @DisplayName("사용자의 포인트 내역 조회")
    void 사용자_포인트_내역_조회() {
        //Given
        long userId = 1L;
        List<PointHistory> expectedPointHistories = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 500L, TransactionType.USE, System.currentTimeMillis())
        );
        when(pointHistoryReader.read(userId)).thenReturn(expectedPointHistories);

        //when
        List<PointHistory> result = pointService.readHistory(userId);

        //then
        assertEquals(expectedPointHistories, result);
        verify(pointHistoryReader, times(1)).read(userId);

    }

    @Test
    @DisplayName("사용자의 포인트 내역이 존재하지 않을 때")
    void 포인트_내역이_존재하지_않을_때() {
        //Given
        long userId = 1L;
        List<PointHistory> expectedPointHistories = List.of();
        when(pointService.readHistory(userId)).thenReturn(expectedPointHistories);
        when(pointHistoryReader.read(userId)).thenThrow(new PointException("포인트 내역이 존재하지 않습니다."));

        //when
        PointException exception = assertThrows(PointException.class, () -> pointService.readHistory(userId));


        //then
        verify(pointHistoryReader, times(1)).read(userId);

        assertEquals("포인트 내역이 존재하지 않습니다.", exception.getMessage());

    }

    @Test
    @DisplayName("사용자의 포인트를 충전합니다")
    void 포인트_충전() {
        //Given
        long userId = 1L;
        long amount = 500L;
        UserPoint existUserPoint = new UserPoint(userId, 0L, System.currentTimeMillis());
        long existAmount = existUserPoint.point();
        long updateAmount = amount + existAmount;

        UserPoint expectedUserPoint = new UserPoint(userId, updateAmount, System.currentTimeMillis());
        when(pointReader.read(userId)).thenReturn(existUserPoint);
        when(pointManager.charge(userId, updateAmount)).thenReturn(expectedUserPoint);

        //when
        UserPoint result = pointService.charge(userId, amount);

        //then
        verify(pointManager, times(1)).charge(userId, updateAmount);
        assertEquals(expectedUserPoint, result);
    }

    @Test
    @DisplayName("포인트를 사용합니다.")
    void use_shouldUpdateUserPoint() {
        //given
        long userId = 1L;
        long amount = 500L;
        UserPoint existUserPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());
        long existAmount = existUserPoint.point();
        long updateAmount = existAmount - amount;
        UserPoint expectedUserPoint = new UserPoint(userId, updateAmount, System.currentTimeMillis());
        when(pointReader.read(userId)).thenReturn(existUserPoint);
        when(pointManager.use(userId, amount)).thenReturn(expectedUserPoint);

        //when
        UserPoint result = pointService.use(userId, amount);

        //then
        verify(pointManager, times(1))
                .use(eq(userId), eq(amount));
        verify(pointHistoryManager, times(1))
                .append(eq(userId), eq(amount), eq(TransactionType.USE), anyLong());
        assertEquals(expectedUserPoint, result);
    }

    @Test
    @DisplayName("보유 포인트보다 사용할 포인트가 많을 때 예외")
    void 보유_포인트보다_사용할_포인트가_많을_때() {
        //given
        long userId = 1L;
        long useAmount = 500L;
        when(pointManager.use(userId, useAmount)).thenThrow(new PointException("사용할 포인트가 부족합니다."));
        when(pointService.read(userId)).thenReturn(new UserPoint(userId, 1000L, System.currentTimeMillis()));
        //when
        PointException exception = assertThrows(PointException.class, () -> pointService.use(userId, useAmount));

        verify(pointManager, times(1)).use(userId, useAmount);
        verify(pointHistoryManager, never()).append(anyLong(), anyLong(), any(), anyLong());

        //then
        assertEquals("사용할 포인트가 부족합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용할 포인트가 음수일 때")
    void 사용할_포인트가_음수일_때() {
        //given
        long userId = 1L;
        long useAmount = -500L;
        when(pointManager.use(userId, useAmount)).thenThrow(new PointException("적립할 포인트는 0보다 커야 합니다."));
        when(pointService.read(userId)).thenReturn(new UserPoint(userId, 1000L, System.currentTimeMillis()));
        //when
        PointException exception = assertThrows(PointException.class, () -> pointService.use(userId, useAmount));

        verify(pointManager, times(1)).use(userId, useAmount);
        verify(pointHistoryManager, never()).append(anyLong(), anyLong(), any(), anyLong());

        //then
        assertEquals("적립할 포인트는 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("최대 잔고를 넘겼을 때")
    void 최대_잔고를_넘겼을_때() {
        //given
        long userId = 1L;
        long chargeAmount = 100000L;
        when(pointService.read(userId)).thenReturn(new UserPoint(userId, 1000L, System.currentTimeMillis()));
        when(pointManager.charge(userId, chargeAmount)).thenThrow(new PointException("최대 잔고는 99999를 넘을 수 없습니다."));
        //when
        PointException exception = assertThrows(PointException.class, () -> pointService.charge(userId, chargeAmount));

        verify(pointManager, times(1)).charge(userId, chargeAmount);
        verify(pointHistoryManager, never()).append(anyLong(), anyLong(), any(), anyLong());

        //then
        assertEquals("최대 잔고는 99999를 넘을 수 없습니다.", exception.getMessage());
    }


}