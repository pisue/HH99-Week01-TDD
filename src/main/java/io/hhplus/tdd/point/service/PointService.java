package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.PointHistory;
import io.hhplus.tdd.point.entity.UserPoint;
import io.hhplus.tdd.point.service.reader.PointHistoryReader;
import io.hhplus.tdd.point.service.reader.PointReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointReader pointReader;
    private final PointHistoryReader pointHistoryReader;

    public UserPoint read(long id) {
        return pointReader.read(id);
    }

    public List<PointHistory> readHistory(long id) {
        return pointHistoryReader.read(id);
    }
}
