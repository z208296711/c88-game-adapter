package com.c88.game.adapter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.game.adapter.pojo.entity.MemberGameSession;
import com.c88.game.adapter.service.IMemberGameSessionService;
import com.c88.game.adapter.mapper.MemberGameSessionMapper;
import org.springframework.stereotype.Service;

/**
* @author user
* @description 针对表【ga_member_game_session(會員登入遊戲紀錄)】的数据库操作Service实现
* @createDate 2022-05-18 13:54:06
*/
@Service
public class MemberGameSessionServiceImpl extends ServiceImpl<MemberGameSessionMapper, MemberGameSession>
    implements IMemberGameSessionService {

}




