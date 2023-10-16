package com.c88.game.adapter.repository;

import com.c88.game.adapter.pojo.document.PlatformGameDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IPlatformGameRepository extends ElasticsearchRepository<PlatformGameDocument, Integer> {

}