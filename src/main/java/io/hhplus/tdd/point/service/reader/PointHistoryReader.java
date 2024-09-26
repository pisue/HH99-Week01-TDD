package io.hhplus.tdd.point.service.reader;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.exception.PointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PointHistoryReader {
    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> read(long id) {
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(id);
        if (pointHistories.isEmpty()) {
            throw new PointException("포인트 내역이 존재하지 않습니다.");
        }
        return pointHistories;
    }
}
