package com.c88.game.adapter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.c88.game.adapter.pojo.entity.GameCategory;
import com.c88.game.adapter.pojo.form.ModifyGameCategoryNoteForm;
import com.c88.game.adapter.pojo.form.ModifyGameCategorySortForm;
import com.c88.game.adapter.pojo.vo.GameCategoryVO;
import com.c88.member.vo.OptionVO;

import java.util.List;

/**
 * @author user
 * @description 针对表【game_template(遊戲類型)】的数据库操作Service
 * @createDate 2022-05-10 13:44:33
 */
public interface IGameCategoryService extends IService<GameCategory> {

    List<GameCategoryVO> findGameCategory();

    Boolean modifyGameCategorySort(ModifyGameCategorySortForm form);

    Boolean modifyGameCategoryNote(ModifyGameCategoryNoteForm form);

    List<OptionVO> getGameCategoryByGame(int id);
}
