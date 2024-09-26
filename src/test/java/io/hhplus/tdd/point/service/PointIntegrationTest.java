package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.service.manager.PointHistoryManager;
import io.hhplus.tdd.point.service.manager.PointManager;
import io.hhplus.tdd.point.service.reader.PointHistoryReader;
import io.hhplus.tdd.point.service.reader.PointReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
public class PointIntegrationTest {
    @Autowired
    private PointService pointService;

    @Autowired
    private PointReader pointReader;

    @Autowired
    private PointHistoryReader pointHistoryReader;

    @Autowired
    private PointManager pointManager;

    @Autowired
    private PointHistoryManager pointHistoryManager;

    @Test
    @DisplayName("동시에 여러 요청이 들어와도 포인트가 정확히 충전됩니다")
    void 동시에_여러_요청이_들어와도_포인트가_정확히_충전됩니다() throws InterruptedException {
        // Given
        long userId = 1L;
        int threadCount = 10;
        long chargeAmount = 100L;

        // 초기 포인트가 없는 상태에서 처음으로 포인트를 충전하는 방식
        // 동시성 테스트를 위해 여러 스레드가 동시에 충전 요청을 보냅니다.

        // When
        List<CompletableFuture<UserPoint>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            // supplyAsync()로 비동기 작업 생성
            CompletableFuture<UserPoint> future = CompletableFuture.supplyAsync(() -> pointService.charge(userId, chargeAmount));
            futures.add(future);
        }

        // 모든 CompletableFuture가 완료될 때까지 대기
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // 모든 비동기 작업이 완료될 때까지 대기

        // Then
        // 포인트 충전이 완료된 후 최종 포인트 값 확인
        UserPoint result = pointService.read(userId);
        assertEquals(chargeAmount * threadCount, result.point(), "동시에 여러 요청이 들어와도 포인트가 정확히 충전되어야 합니다.");
    }

    @Test
    @DisplayName("동시에 여러 요청이 들어와도 포인트가 정확히 사용됩니다")
    void 동시에_여러_요청이_들어와도_포인트가_정확히_사용됩니다() throws InterruptedException {
        // Given
        long userId = 1L;
        long initialAmount = 1100L; // 초기 포인트
        long useAmount = 100L; // 사용할 포인트
        int threadCount = 10; // 스레드 수

        // 초기 포인트를 설정 (예: 데이터베이스 또는 in-memory 저장소에 삽입)
        pointService.charge(userId, initialAmount);

        // When
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<UserPoint>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            // 여러 스레드에서 포인트를 사용하는 비동기 작업을 생성
            CompletableFuture<UserPoint> future = CompletableFuture.supplyAsync(() -> pointService.use(userId, useAmount), executorService);
            futures.add(future);
        }

        // 모든 CompletableFuture가 완료될 때까지 대기
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // 모든 비동기 작업이 완료될 때까지 대기

        // Then
        // 최종 포인트 값 확인
        UserPoint result = pointService.read(userId);
        long expectedAmount = initialAmount - (useAmount * threadCount);
        assertTrue(result.point() >= 0, "포인트는 0 이상이어야 합니다.");
        assertEquals(expectedAmount, result.point(), "포인트 사용 후 예상되는 잔액과 일치해야 합니다.");
    }

}
