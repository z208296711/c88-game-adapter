package com.c88.game.adapter;

import com.c88.game.adapter.constants.Tokenizer;
import com.c88.game.adapter.mapstruct.PlatformGameToRepositoryConverter;
import com.c88.game.adapter.pojo.document.BetOrderDocument;
import com.c88.game.adapter.pojo.document.PlatformGameDocument;
import com.c88.game.adapter.pojo.entity.PlatformGame;
import com.c88.game.adapter.repository.IBetOrderRepository;
import com.c88.game.adapter.repository.IPlatformGameRepository;
import com.c88.game.adapter.service.IPlatformGameService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.StatsAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.PutTemplateRequest;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest(properties = "spring.profiles.active:local")
class GameAdapterESKeywordTests {

    @Resource
    private IPlatformGameService iPlatformGameService;

    @Resource
    private PlatformGameToRepositoryConverter platformGameToRepositoryConverter;

    @Resource
    private IPlatformGameRepository iPlatformGameRepository;

    @Resource
    private ElasticsearchOperations operations;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private IBetOrderRepository iBetOrderRepository;

    private static final String PLATFORM_GAME = "platform-game";

    private static final String BET_ORDER = "bet-order-202205";

    private static final String PLATFORM_GAME_TEMPLATE = "platform-game-template";

    private static final String[] INDICES = new String[]{"platform-game-vi", "platform-game-en"};

    private static final String[] ALIASES = new String[]{"platform-game"};

    /**
     * 產生platform game template
     */
    @Test
    void InitPlatformGame() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(PlatformGameDocument.class);
        if (indexOperations.existsTemplate(PLATFORM_GAME_TEMPLATE)) {
            log.info("存在Template");
            indexOperations.deleteTemplate(PLATFORM_GAME_TEMPLATE);
        }
        log.info("gen template start");

        AliasActionParameters aliasActionParameters = AliasActionParameters.builderForTemplate()
                .withIndices(INDICES)
                .withAliases(ALIASES)
                .build();

        AliasAction alias = new AliasAction.Add(aliasActionParameters);
        AliasActions aliasActions = new AliasActions().add(alias);

        PutTemplateRequest putTemplateRequest = PutTemplateRequest.builder(PLATFORM_GAME_TEMPLATE, PLATFORM_GAME)
                // .withAliasActions(aliasActions)
                .withMappings(Document.create())
                .build();
        boolean b = indexOperations.putTemplate(putTemplateRequest);

        System.out.println(indexOperations.alias(aliasActions));

        log.info(String.valueOf(b));

        log.info("gen template end");
    }

    /**
     * 更新全部平台遊戲
     */
    @Test
    void updateAllPlatformGame() {
        // iPlatformGameRepository.deleteAll();
        List<PlatformGame> platformGames = iPlatformGameService.list();
        List<PlatformGameDocument> platformGameDocuments = platformGames.stream().map(platformGameToRepositoryConverter::toVo).collect(Collectors.toUnmodifiableList());
        iPlatformGameRepository.saveAll(platformGameDocuments);
    }

    /**
     * 查詢注單
     */
    @Test
    void testBetRangeSearch() {
        String searchStr = "transactionTime";
        QueryBuilder queryBuilderCh =
                QueryBuilders.rangeQuery(searchStr)
                        .from("2022-05-01")
                        .to("2022-06-30")
                        .format("yyyy-MM-dd");

        Query query = new NativeSearchQueryBuilder()
                .withFilter(queryBuilderCh)
                .build();

        SearchHits<BetOrderDocument> search =
                elasticsearchRestTemplate.search(query, BetOrderDocument.class,IndexCoordinates.of("bet-order"));

        Set<String> completeKeys = search.stream().map(x -> x.getContent().getTransactionNo()).collect(Collectors.toUnmodifiableSet());

        log.info(String.valueOf(completeKeys.size()));
        log.info(completeKeys.toString());
    }

    /**
     * 別名分詞查詢
     */
    @Test
    void testTokenizerAlias() {
        String searchStr = "财";
        QueryBuilder queryBuilderCh =
                new QueryStringQueryBuilder(searchStr)
                        .analyzer(Tokenizer.ICU_ANALYZER)
                        .field("nameVi");

        Query query = new NativeSearchQueryBuilder()
                .withFilter(queryBuilderCh)
                .build();

        SearchHits<PlatformGameDocument> search =
                elasticsearchRestTemplate.search(query, PlatformGameDocument.class, IndexCoordinates.of(PLATFORM_GAME));

        Set<String> completeKeys = search.stream().map(x -> x.getContent().getNameVi()).collect(Collectors.toUnmodifiableSet());

        log.info(completeKeys.toString());
    }

    /**
     * 多語言分詞
     * 使用同样的 Unicode 文本分段算法,支持泰语、老挝语、中文、日文、和韩文
     */
    @Test
    void testTokenizerMultiAsia() {
        String searchStr = "财";
        QueryBuilder queryBuilderCh =
                new QueryStringQueryBuilder(searchStr)
                        .analyzer(Tokenizer.ICU_ANALYZER)
                        .field("nameVi");

        Query query = new NativeSearchQueryBuilder()
                .withFilter(queryBuilderCh)
                .build();

        SearchHits<PlatformGameDocument> search =
                elasticsearchRestTemplate.search(query, PlatformGameDocument.class, IndexCoordinates.of(PLATFORM_GAME));

        Set<String> completeKeys = search.stream().map(x -> x.getContent().getNameVi()).collect(Collectors.toUnmodifiableSet());

        log.info(completeKeys.toString());
    }

    /**
     * 越南語分詞
     */
    @Test
    void testTokenizerICU() {
        String searchStr = "Xô";
        QueryBuilder queryBuilderCh =
                new QueryStringQueryBuilder(searchStr)
                        .analyzer(Tokenizer.VI_ANALYZER)
                        .field("nameVi");

        Query query = new NativeSearchQueryBuilder()
                .withFilter(queryBuilderCh)
                .build();

        SearchHits<PlatformGameDocument> search =
                elasticsearchRestTemplate.search(query, PlatformGameDocument.class, IndexCoordinates.of(PLATFORM_GAME));

        Set<String> completeKeys = search.stream().map(x -> x.getContent().getNameVi()).collect(Collectors.toUnmodifiableSet());

        log.info(completeKeys.toString());
    }

    /**
     * 模糊搜尋
     */
    @Test
    void testFuzzy() {
        QueryBuilder queryBuilder =
                QueryBuilders
                        .multiMatchQuery("name-AAAAAA", "name", "englishName")
                        .fuzziness(Fuzziness.AUTO);

        Query query = new NativeSearchQueryBuilder()
                .withFilter(queryBuilder)
                .withSorts(SortBuilders.fieldSort("gameSort"))
                .build();

        SearchHits<PlatformGameDocument> search =
                operations.search(query, PlatformGameDocument.class, IndexCoordinates.of(PLATFORM_GAME));

        Set<String> completeKeys = search.stream().map(x -> x.getContent().getNameVi()).collect(Collectors.toUnmodifiableSet());

        log.info(completeKeys.toString());
    }

    /**
     * 建議搜尋
     */
    @Test
    void testSuggestions() {
        WildcardQueryBuilder queryBuilder = QueryBuilders.wildcardQuery("nameEn", "*aaa eee*");

        Query query = new NativeSearchQueryBuilder()
                .withFilter(queryBuilder)
                .build();

        SearchHits<PlatformGameDocument> search =
                operations.search(query, PlatformGameDocument.class, IndexCoordinates.of(PLATFORM_GAME));

        Set<String> completeKeys = search.stream().map(x -> x.getContent().getNameVi()).collect(Collectors.toUnmodifiableSet());

        log.info(completeKeys.toString());
    }

    /**
     * 多項目Wildcard搜尋
     */
    @Test
    void testMultiWildcardQuery() {
        String keyword = "KTV";
        Integer userDriver = 0;

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.should(QueryBuilders.wildcardQuery("nameVi", "*" + keyword + "*"));
        boolQueryBuilder.should(QueryBuilders.wildcardQuery("nameEn", "*" + keyword + "*"));
        boolQueryBuilder.should(QueryBuilders.wildcardQuery("tags", "*" + keyword + "*"));

        Query query = new NativeSearchQueryBuilder()
                .withFilter(boolQueryBuilder)
                .withQuery(QueryBuilders.matchQuery(userDriver == 0 ? "pcDeviceVisible" : "h5DeviceVisible", "1"))
                .withPageable(PageRequest.of(0, 1))
                .build();

        SearchHits<PlatformGameDocument> search =
                operations.search(query, PlatformGameDocument.class, IndexCoordinates.of(PLATFORM_GAME));

        Set<String> completeKeys = search.stream().map(x -> x.getContent().getNameVi()).collect(Collectors.toUnmodifiableSet());

        log.info(completeKeys.toString());
    }

    @Test
    void testAdd() {
        PlatformGameDocument p1 =
                PlatformGameDocument.builder()
                        .id(1)
                        .nameVi("name-AAAAAA")
                        .nameEn("eName-Test")
                        .platformId(1)
                        .gameCategoryId(1)
                        .gameSort(1)
                        .platformName("pName-Test")
                        .build();

        PlatformGameDocument p2 =
                PlatformGameDocument.builder()
                        .id(2)
                        .nameVi("name-BBBBBBB")
                        .nameEn("eName-Test")
                        .platformId(1)
                        .gameCategoryId(1)
                        .gameSort(2)
                        .platformName("pName-Test")
                        .build();

        PlatformGameDocument p3 =
                PlatformGameDocument.builder()
                        .id(3)
                        .nameVi("name-AAAAAAC")
                        .nameEn("eName-Test")
                        .platformId(1)
                        .platformName("pName-Test")
                        .gameSort(3)
                        .gameCategoryId(1)
                        .build();

        PlatformGameDocument p4 =
                PlatformGameDocument.builder()
                        .id(4)
                        .nameVi("name-AAAAAACD")
                        .nameEn("eName-Test")
                        .platformId(1)
                        .platformName("pName-Test")
                        .gameSort(4)
                        .gameCategoryId(1)
                        .build();

        PlatformGameDocument p5 =
                PlatformGameDocument.builder()
                        .id(5)
                        .nameVi("name-AAAAAACDE")
                        .nameEn("eName-Test")
                        .platformId(1)
                        .platformName("pName-Test")
                        .gameSort(5)
                        .gameCategoryId(1)
                        .build();

        iPlatformGameRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5));

        Iterable<PlatformGameDocument> all = iPlatformGameRepository.findAll();
        all.forEach(x -> log.info(x.toString()));
    }


}
