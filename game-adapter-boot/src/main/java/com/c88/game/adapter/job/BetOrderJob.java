package com.c88.game.adapter.job;

import com.c88.game.adapter.constants.AELotteryConstants;
import com.c88.game.adapter.service.third.GameAdapterExecutor;
import com.c88.game.adapter.service.third.adapter.*;
import com.c88.game.adapter.service.third.v8.V8GameAdapter;
import com.c88.game.adapter.utils.DateUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
@RequiredArgsConstructor
public class BetOrderJob {

    private final KAGameAdapter kaGameAdapter;

    private final CMDGameAdapter cmdGameAdapter;

    private final AELiveGameAdapter aeGameAdapter;

    private final CQ9GameAdapter cq9GameAdapter;

    private final SABAGameAdapter sabaGameAdapter;

    private final V8GameAdapter v8GameAdapter;

    private final GameAdapterExecutor gameAdapterExecutor;

    private final PPGameAdapter ppGameAdapter;

    private final TCGameAdapter tcGameAdapter;

    private final MPGameAdapter mpGameAdapter;
    private final FCGameAdapter fcGameAdapter;
    private final LYFDGameAdapter lyfdGameAdapter;

    private final PSGameAdapter psGameAdapter;

    private final JILIGameAdapter jiliGameAdapter;

    private final EVOGameAdapter evoGameAdapter;

    private final DS88GameAdapter ds88GameAdapter;
    private final PGGameAdapter pgGameAdapter;

    @XxlJob("fetchV8BetOrder")
    public void fetchV8BetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchV8BetOrder");
        // 获取参数
        String params = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-mm-dd hh:mm:ss";
        if (StringUtils.isNotBlank(params)) {
            List<String> param = Arrays.asList(params.split(","));
            v8GameAdapter.manualBetOrder(DateUtil.toLocalDateTime(param.get(0), datePattern),
                    DateUtil.toLocalDateTime(param.get(1), datePattern), null);
        } else {
            v8GameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchCMDBetOrder")
    public void fetchCMDBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchCMDBetOrder");
        // 获取参数
        String version = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(version)) {
            cmdGameAdapter.manualBetOrder(null, null, Map.of("version", version));
        } else {
            cmdGameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchAELotteryBetOrder")
    public void fetchAEBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchAELotteryBetOrder");

        IGameAdapter gameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(AELotteryConstants.PLATFORM_CODE);

        // 获取参数
        String param = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            gameAdapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), null);
        } else {
            gameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchCQ9BetOrder")
    public void fetchCQ9BetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchCQ9BetOrder");
        // 获取参数
        String param = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-mm-dd hh:mm:ss";
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            Map<String, String> map = new HashMap<>();
            map.put("page", params.get(2));
            cq9GameAdapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), map);
        } else {
            cq9GameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchKABetOrder")
    public void fetchKABetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchKABetOrder");
        // 获取参数
        String param = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            kaGameAdapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), null);
        } else {
            kaGameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchSABABetOrder")
    public void fetchSABABetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchSABABetOrder");
        // 获取参数
        String version = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(version)) {
            sabaGameAdapter.manualBetOrder(null, null, Map.of("version", version));
        } else {
            sabaGameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchPPBetOrder")
    public void fetchPPBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchPPBetOrder");
        // 获取参数
        String param = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            ppGameAdapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), null);
        } else {
            ppGameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchTCBetOrder")
    public void fetchTCBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchTCBetOrder");

        String param = XxlJobHelper.getJobParam();
        mainAction(tcGameAdapter,param);
    }

    @XxlJob("fetchMPBetOrder")
    public void fetchMPBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchMPBetOrder");

        String param = XxlJobHelper.getJobParam();
        mainAction(mpGameAdapter,param);
    }

    @XxlJob("fetchPSBetOrder")
    public void fetchPSBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchPSBetOrder");

        String param = XxlJobHelper.getJobParam();
        mainAction(psGameAdapter,param);
    }

    private void mainAction(IGameAdapter adapter,String param){
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            adapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), null);
        } else {
            adapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchFCBetOrder")
    public void fetchFCBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchFCBetOrder");
        // 获取参数
        String version = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(version)) {
            fcGameAdapter.manualBetOrder(null, null, Map.of("version", version));
        } else {
            fcGameAdapter.doFetchBetOrderAction();

        }
    }

    @XxlJob("fetchLYFDBetOrder")
    public void fetchLYFDBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchSLYFDetOrder");
        // 获取参数
        String version = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(version)) {
            lyfdGameAdapter.manualBetOrder(null, null, Map.of("version", version));
        } else {
            lyfdGameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchJILIBetOrder")
    public void fetchJILIBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchJILIBetOrder");
        // 获取参数
        String param = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        log.info("fetchJILIBetOrder param : {}", param);
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            jiliGameAdapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), null);
        } else {
            jiliGameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchEVOBetOrder")
    public void fetchEVOBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchEVOBetOrder");
        // 获取参数
        String param = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        log.info("fetchEVOBetOrder param : {}", param);
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            evoGameAdapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), null);
        } else {
            evoGameAdapter.doFetchBetOrderAction();
        }
    }

    @XxlJob("fetchDS88BetOrder")
    public void fetchDS88BetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchDS88BetOrder");
        // 获取参数
        String param = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            ds88GameAdapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), null);
        } else {
            ds88GameAdapter.doFetchBetOrderAction();
        }
    }
    
    @XxlJob("fetchPGBetOrder")
    public void fetchPGBetOrder() {
        XxlJobHelper.log("XXL-JOB, fetchPGBetOrder");
        // 获取参数
        String param = XxlJobHelper.getJobParam();
        String datePattern = "yyyy-MM-dd HH:mm:ss";
        log.info("fetchPGBetOrder param : {}", param);
        if (StringUtils.isNotBlank(param)) {
            List<String> params = Arrays.asList(param.split(","));
            pgGameAdapter.manualBetOrder(DateUtil.toLocalDateTime(params.get(0), datePattern),
                    DateUtil.toLocalDateTime(params.get(1), datePattern), null);
        } else {
            pgGameAdapter.doFetchBetOrderAction();
        }
    }
}
