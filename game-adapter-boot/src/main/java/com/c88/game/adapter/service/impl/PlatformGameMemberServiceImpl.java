package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.mapper.PlatformGameMemberMapper;
import com.c88.game.adapter.mapstruct.PlatformGameMemberConverter;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.pojo.form.FindPlatformGameMemberForm;
import com.c88.game.adapter.pojo.vo.PlatformGameMemberVO;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author user
 * @description 针对表【platform_game_member(平台遊戲會員列表)】的数据库操作Service实现
 * @createDate 2022-05-10 13:46:42
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformGameMemberServiceImpl extends ServiceImpl<PlatformGameMemberMapper, PlatformGameMember>
        implements IPlatformGameMemberService {

    private final PlatformGameMemberConverter platformGameMemberConverter;

    public IPage<PlatformGameMemberVO> findPlatformGameMember(FindPlatformGameMemberForm form) {
        return this.lambdaQuery()
                .eq(Objects.nonNull(form.getMemberId()), PlatformGameMember::getMemberId, form.getMemberId())
                .page(new Page<>(form.getPageNum(), form.getPageSize()))
                .convert(platformGameMemberConverter::toVo);
    }

    @Override
    @Cacheable(cacheNames = RedisKey.PLATFORM_GAME_MEMBER, key = "#platform+':'+#username", unless = "#result == null")
    public PlatformGameMember findByPlatformAndUsername(String platform, String username) {
        return this.lambdaQuery()
                .eq(PlatformGameMember::getUsername, username)
                .eq(PlatformGameMember::getCode, platform)
                .one();
    }

}




