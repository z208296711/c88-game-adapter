package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.pojo.form.FindPlatformGameMemberForm;
import com.c88.game.adapter.pojo.vo.PlatformGameMemberVO;

/**
 * @author user
 * @description 针对表【platform_game_member(平台遊戲會員列表)】的数据库操作Service
 * @createDate 2022-05-10 13:46:42
 */
public interface IPlatformGameMemberService extends IService<PlatformGameMember> {

    IPage<PlatformGameMemberVO> findPlatformGameMember(FindPlatformGameMemberForm form);

    PlatformGameMember findByPlatformAndUsername(String platform, String username);

}
