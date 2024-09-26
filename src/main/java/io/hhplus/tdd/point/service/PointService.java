package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.TransactionType;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.manager.PointHistoryManager;
import io.hhplus.tdd.point.service.manager.PointManager;
import io.hhplus.tdd.point.service.reader.PointHistoryReader;
import io.hhplus.tdd.point.service.reader.PointReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointReader pointReader;
    private final PointHistoryReader pointHistoryReader;
    private final PointManager pointManager;
    private final PointHistoryManager pointHistoryManager;

    private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    public UserPoint read(long id) {
        return pointReader.read(id);
    }

    public List<PointHistory> readHistory(long id) {
        return pointHistoryReader.read(id);
    }

    public UserPoint charge(long id, long amount) {
        //사용자별로 동적 락을 사용하는 방식
        Lock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();
        try {
            final UserPoint updatedUserPoint = pointManager.charge(id, amount);
            pointHistoryManager.append(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }

    public UserPoint use(long id, long amount) {
        //사용자별로 동적 락을 사용하는 방식
        Lock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();
        try {
            final UserPoint updatedUserPoint = pointManager.use(id, amount);
            pointHistoryManager.append(id, amount, TransactionType.USE, System.currentTimeMillis());
            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }

}
