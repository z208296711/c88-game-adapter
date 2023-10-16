package com.c88.game.adapter.job;

import com.c88.game.adapter.enums.PlatformMaintainStateEnum;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.service.IPlatformService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.c88.game.adapter.enums.PlatformMaintainStateEnum.MAINTAIN_END;
import static com.c88.game.adapter.enums.PlatformMaintainStateEnum.MAINTAIN_START;

@Component
@RequiredArgsConstructor
public class PlatformMaintainJob {

    private final IPlatformService iPlatformService;

    /**
     * CMD維護啟用
     */
    @XxlJob("maintainStartByCMD")
    public void maintainStartByCMD() {
        modifyMaintainStateByPlatformCode("CMD", MAINTAIN_START);
    }

    /**
     * CMD維護停用
     */
    @XxlJob("maintainEndByCMD")
    public void maintainEndByCMD() {
        modifyMaintainStateByPlatformCode("CMD", MAINTAIN_END);
    }

    /**
     * SBA維護啟用
     */
    @XxlJob("maintainStartBySBA")
    public void maintainStartBySBA() {
        modifyMaintainStateByPlatformCode("SBA", MAINTAIN_START);
    }

    /**
     * SBA維護停用
     */
    @XxlJob("maintainEndBySBA")
    public void maintainEndBySBA() {
        modifyMaintainStateByPlatformCode("SBA", MAINTAIN_END);
    }

    /**
     * SABONG維護啟用
     */
    @XxlJob("maintainStartBySABONG")
    public void maintainStartBySABONG() {
        modifyMaintainStateByPlatformCode("SABONG", MAINTAIN_START);
    }

    /**
     * SABONG維護停用
     */
    @XxlJob("maintainEndBySABONG")
    public void maintainEndBySABONG() {
        modifyMaintainStateByPlatformCode("SABONG", MAINTAIN_END);
    }

    /**
     * KA維護啟用
     */
    @XxlJob("maintainStartByKA")
    public void maintainStartByKA() {
        modifyMaintainStateByPlatformCode("KA", MAINTAIN_START);
    }

    /**
     * KA維護停用
     */
    @XxlJob("maintainEndByKA")
    public void maintainEndByKA() {
        modifyMaintainStateByPlatformCode("KA", MAINTAIN_END);
    }

    /**
     * PNG維護啟用
     */
    @XxlJob("maintainStartByPNG")
    public void maintainStartByPNG() {
        modifyMaintainStateByPlatformCode("PNG", MAINTAIN_START);
    }

    /**
     * PNG維護停用
     */
    @XxlJob("maintainEndByPNG")
    public void maintainEndByPNG() {
        modifyMaintainStateByPlatformCode("PNG", MAINTAIN_END);
    }

    /**
     * V8維護啟用
     */
    @XxlJob("maintainStartByV8")
    public void maintainStartByV8() {
        modifyMaintainStateByPlatformCode("V8", MAINTAIN_START);
    }

    /**
     * V8維護停用
     */
    @XxlJob("maintainEndByV8")
    public void maintainEndByV8() {
        modifyMaintainStateByPlatformCode("V8", MAINTAIN_END);
    }

    /**
     * TC維護啟用
     */
    @XxlJob("maintainStartByTC")
    public void maintainStartByTCG() {
        modifyMaintainStateByPlatformCode("TC", MAINTAIN_START);
    }

    /**
     * TC維護停用
     */
    @XxlJob("maintainEndByTC")
    public void maintainEndByTCG() {
        modifyMaintainStateByPlatformCode("TC", MAINTAIN_END);
    }

    /**
     * LYFD維護啟用
     */
    @XxlJob("maintainStartByLYFD")
    public void maintainStartByLYFD() {
        modifyMaintainStateByPlatformCode("LYFD", MAINTAIN_START);
    }

    /**
     * LYFD維護停用
     */
    @XxlJob("maintainEndByLYFD")
    public void maintainEndByLYFD() {
        modifyMaintainStateByPlatformCode("LYFD", MAINTAIN_END);
    }

    /**
     * FC維護啟用
     */
    @XxlJob("maintainStartByFC")
    public void maintainStartByFC() {
        modifyMaintainStateByPlatformCode("FC", MAINTAIN_START);
    }

    /**
     * LYFD維護停用
     */
    @XxlJob("maintainEndByFC")
    public void maintainEndByFC() {
        modifyMaintainStateByPlatformCode("FC", MAINTAIN_END);
    }

    /**
     * JILI維護啟用
     */
    @XxlJob("maintainStartByJILI")
    public void maintainStartByJILI() {
        modifyMaintainStateByPlatformCode("JILI", MAINTAIN_START);
    }

    /**
     * JILI維護停用
     */
    @XxlJob("maintainEndByJILI")
    public void maintainEndByJILI() {
        modifyMaintainStateByPlatformCode("JILI", MAINTAIN_END);
    }

    /**
     * EVO維護啟用
     */
    @XxlJob("maintainStartByEVO")
    public void maintainStartByEVO() {
        modifyMaintainStateByPlatformCode("EVO", MAINTAIN_START);
    }

    /**
     * EVO維護停用
     */
    @XxlJob("maintainEndByEVO")
    public void maintainEndByEVO() {
        modifyMaintainStateByPlatformCode("EVO", MAINTAIN_END);
    }

    /**
     * PS維護啟用
     */
    @XxlJob("maintainStartByPS")
    public void maintainStartByPS() {
        modifyMaintainStateByPlatformCode("PS", MAINTAIN_START);
    }

    /**
     * PS維護停用
     */
    @XxlJob("maintainEndByPS")
    public void maintainEndByPS() {
        modifyMaintainStateByPlatformCode("PS", MAINTAIN_END);
    }

    /**
     * MP維護啟用
     */
    @XxlJob("maintainStartByMP")
    public void maintainStartByMP() {
        modifyMaintainStateByPlatformCode("MP", MAINTAIN_START);
    }

    /**
     * MO維護停用
     */
    @XxlJob("maintainEndByMP")
    public void maintainEndByMP() {
        modifyMaintainStateByPlatformCode("MP", MAINTAIN_END);
    }

    /**
     * Pinnacle維護啟用
     */
    @XxlJob("maintainStartByPinnacle")
    public void maintainStartByPinnacle() {
        modifyMaintainStateByPlatformCode("Pinnacle", MAINTAIN_START);
    }

    /**
     * Pinnacle維護停用
     */
    @XxlJob("maintainEndByPinnacle")
    public void maintainEndByPinnacle() {
        modifyMaintainStateByPlatformCode("Pinnacle", MAINTAIN_END);
    }


    /**
     * 修改平台維護狀態By平台代碼
     *
     * @param platformCode              平台代碼
     * @param platformMaintainStateEnum 平台維護狀態Enum
     */
    private void modifyMaintainStateByPlatformCode(String platformCode, PlatformMaintainStateEnum platformMaintainStateEnum) {
        iPlatformService.lambdaUpdate()
                .eq(Platform::getCode, platformCode)
                .set(Platform::getMaintainState, platformMaintainStateEnum.getCode())
                .update();
    }

}
