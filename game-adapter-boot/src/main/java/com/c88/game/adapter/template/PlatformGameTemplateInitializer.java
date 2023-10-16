package com.c88.game.adapter.template;

import com.c88.game.adapter.pojo.document.PlatformGameDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.index.PutTemplateRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformGameTemplateInitializer {

    private static final String TEMPLATE_NAME = "platform-game-template";

    private static final String TEMPLATE_PATTERN = "platform-game";

    private final ElasticsearchOperations operations;

    @PostConstruct
    public void init() {
        var indexOps = operations.indexOps(PlatformGameDocument.class);
//        indexOps.deleteTemplate(TEMPLATE_NAME);
        if (!indexOps.existsTemplate(TEMPLATE_NAME)) {

            var mapping = indexOps.createMapping();

            var settings = indexOps.createSettings();

            Map<String, Object> myNormalizer = Map.of(
                    "type", "custom",
                    "char_filter", new ArrayList<>(),
                    "filter", List.of("lowercase", "asciifolding"));
            Map<String, Object> normalizer = Map.of("my_normalizer", myNormalizer);
            Map<String, Object> normalizerMap = Map.of("normalizer", normalizer);
            Map<String, Object> analysisMap = Map.of("analysis", normalizerMap);
            settings.putAll(analysisMap);
//            var aliasActions = new AliasActions().add(
//                    new AliasAction.Add(AliasActionParameters.builderForTemplate()
//                            .withAliases(indexOps.getIndexCoordinates().getIndexNames())
//                            .build())
//            );
            var request = PutTemplateRequest.builder(TEMPLATE_NAME, TEMPLATE_PATTERN)
                    .withMappings(mapping)
                    .withSettings(settings)
//                    .withAliasActions(aliasActions)
                    .build();
            indexOps.putTemplate(request);
        }
    }
}
