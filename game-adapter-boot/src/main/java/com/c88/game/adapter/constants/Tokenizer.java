package com.c88.game.adapter.constants;

public class Tokenizer {

    private Tokenizer() {
        throw new IllegalStateException("Utility class");
    }

    // ICU https://www.elastic.co/guide/cn/elasticsearch/guide/current/icu-tokenizer.html
    public static final String ICU_ANALYZER = "icu_analyzer";


    public static final String STANDARD_ANALYZER = "standard";
    public static final String STANDARD_SIMPLE = "simple";
    public static final String STANDARD_LOWERCASE = "lowercase";

    // Vietnamese https://github.com/duydo/elasticsearch-analysis-vietnamese
    public static final String VI_ANALYZER = "vi_analyzer";
    public static final String VI_TOKENIZER = "vi_tokenizer";
    public static final String VI_STOP = "vi_stop";

}
