package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.common.core.base.BasePageQuery;
import com.c88.game.adapter.dto.PlatformDTO;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.form.ModifyPlatformForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformGameSortForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortHotGameForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortTopBottomForm;
import com.c88.game.adapter.pojo.vo.PlatformRateVO;
import com.c88.game.adapter.pojo.vo.PlatformSortVO;
import com.c88.game.adapter.pojo.vo.PlatformVO;
import com.c88.member.vo.OptionVO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author user
 * @description 针对表【platform(遊戲平台)】的数据库操作Service
 * @createDate 2022-05-10 13:46:31
 */
public interface IPlatformService extends IService<Platform> {

    IPage<PlatformVO> findPlatform(BasePageQuery form);

    Boolean modifyPlatform(ModifyPlatformForm form);

    Platform getPlatformById(Long id);

    String uploadImage(MultipartFile file);

    List<PlatformSortVO> findPlatformSort(Integer gameCategoryId);

    Boolean modifyPlatformSort(ModifyPlatformSortForm form);

    Boolean modifyPlatformSortTopBottom(ModifyPlatformSortTopBottomForm form);

    List<OptionVO<Long>> findPlatformOption();

    List<OptionVO<String>> findPlatformCodeOption();

    List<PlatformRateVO> findPlatformRate();

    Boolean modifyPlatformRate(Map<Long, BigDecimal> map);

    List<PlatformDTO> findAllPlatformDTO();

    List<PlatformSortVO> findPlatformSortByHot();

    Boolean modifyPlatformSortByHot(ModifyPlatformGameSortForm form);

    Boolean modifyPlatformSortTopBottomByHot(ModifyPlatformSortTopBottomForm form);

    Boolean modifyPlatformSortHotGame(ModifyPlatformSortHotGameForm form);
}
