package com.c88.game.adapter.repository.impl;

import com.c88.game.adapter.pojo.document.BetOrderDocument;
import com.c88.game.adapter.repository.IBetOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BetOrderRepositoryImpl implements IBetOrderRepository<BetOrderDocument> {

    private final ElasticsearchOperations elasticsearchOperations;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM");

    @Override
    public <S extends BetOrderDocument> S save(S entity) {
        return elasticsearchOperations.save(entity, indexName(entity.getTransactionTime()));
    }

    @Override
    public <S extends BetOrderDocument> void saveAll(List<S> entities) {
        Map<String, List<BetOrderDocument>> map = entities.stream()
                .collect(Collectors.groupingBy(x -> x.getTransactionTime().format(dateTimeFormatter)));
        map.forEach((index, doc) -> elasticsearchOperations.save(entities, IndexCoordinates.of(index)));
    }

    public IndexCoordinates indexName(LocalDateTime dateTime) {
        var indexName = "bet-order-" + dateTime.format(dateTimeFormatter);
        return IndexCoordinates.of(indexName);
    }
}
