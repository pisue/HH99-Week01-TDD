package io.hhplus.tdd.point.service.reader;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.entity.PointHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PointHistoryReader {
    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> read(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }
}
