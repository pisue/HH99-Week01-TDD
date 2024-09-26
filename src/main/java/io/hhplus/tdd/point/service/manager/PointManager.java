package io.hhplus.tdd.point.service.manager;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointManager {
    private final UserPointTable userPointTable;

    public UserPoint charge(Long id, long amount) {
        UserPoint existingUserPoint = userPointTable.selectById(id);
        return userPointTable.insertOrUpdate(id, existingUserPoint.point() + amount);
    }

    public UserPoint use(long id, long amount) {
        UserPoint existingUserPoint = userPointTable.selectById(id);
        return userPointTable.insertOrUpdate(id, existingUserPoint.point() - amount);
    }
}
