package com.c88.game.adapter.template;

import com.c88.game.adapter.pojo.document.BetOrderDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.PutTemplateRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class BetOrderTemplateInitializer {

    private static final String TEMPLATE_NAME = "bet-order-template";

    private static final String TEMPLATE_PATTERN = "bet-order-*";

    private final ElasticsearchOperations operations;

    @PostConstruct
    public void init() {
        var indexOps = operations.indexOps(BetOrderDocument.class);

        if (!indexOps.existsTemplate(TEMPLATE_NAME)) {
            var mapping = indexOps.createMapping();
            var aliasActions = new AliasActions().add(
                    new AliasAction.Add(AliasActionParameters.builderForTemplate()
                            .withAliases(indexOps.getIndexCoordinates().getIndexNames())
                            .build())
            );
            PutTemplateRequest request = PutTemplateRequest.builder(TEMPLATE_NAME, TEMPLATE_PATTERN)
                    .withMappings(mapping)
                    .withAliasActions(aliasActions)
                    .build();

            indexOps.putTemplate(request);
        }

    }
}
