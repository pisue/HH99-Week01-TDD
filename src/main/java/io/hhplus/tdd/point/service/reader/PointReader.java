package io.hhplus.tdd.point.service.reader;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointReader {
    private final UserPointTable userPointTable;

    public UserPoint read(long id) { return userPointTable.selectById(id); }
}
