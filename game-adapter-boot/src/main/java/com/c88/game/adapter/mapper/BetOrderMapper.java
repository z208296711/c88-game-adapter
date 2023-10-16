
package com.c88.game.adapter.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.game.adapter.pojo.entity.BetOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.game.adapter.pojo.entity.MemberBetAmountRecordDaily;
import com.c88.game.adapter.pojo.form.BetAmountRecordForm;
import com.c88.game.adapter.pojo.form.FindMemberAccountBetRecordForm;
import com.c88.game.adapter.pojo.vo.BetOrderDetailRecordVO;
import com.c88.game.adapter.pojo.vo.H5MemberAccountBetDetailRecordVO;
import com.c88.game.adapter.pojo.vo.MemberBetAmountRecordDailyVO;
import com.c88.game.adapter.pojo.vo.TotalCountVO;
import com.c88.game.adapter.pojo.vo.BetOrderRecordVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author user
 * @description 针对表【ga_bet_order(會員注單紀錄)】的数据库操作Mapper
 * @createDate 2022-05-18 11:34:13
 * @Entity com.c88.game.adapter.pojo.entity.BetOrder
 */
public interface BetOrderMapper extends BaseMapper<BetOrder> {

    @Select("SELECT p.name as platform,c.name as category, IFNULL(sum(bet.valid_bet_amount),0) as validBet, ifnull(sum(bet.bet_amount),0) as totalBet, ifnull(sum(bet.win_loss),0) as totalWinLoss " +
            " from ga_bet_order_${tableId} bet" +
            " join ga_platform p on bet.platform_id = p.id" +
            " join ga_game_category c on bet.game_category_code = c.code" +
            " ${ew.customSqlSegment}")
    Page<BetOrderRecordVO> getBetOrderRecord(Page<BetOrderRecordVO> page, @Param("tableId") int tableId, @Param(Constants.WRAPPER) QueryWrapper<BetOrderRecordVO> queryWrapper);

    @Select("SELECT category, totalBetAmount, totalValidBetAmount from(" +
            "SELECT bet.game_category_code as category, IFNULL(sum(bet_amount) ,0) as totalBetAmount, IFNULL(sum(valid_bet_amount),0) as totalValidBetAmount from ga_bet_order_${tableId} bet " +
            " ${ew.customSqlSegment})a " +
            "${sql}")
    List<TotalCountVO> getTotalCount(@Param("tableId") int tableId, @Param(Constants.WRAPPER) QueryWrapper<TotalCountVO> queryWrapper, @Param("sql") String sql);

    @Select("SELECT " +
            "bet.transaction_time AS transaction_time, " +
            "bet.transaction_serial AS transaction_no, " +
            "p.name as platform, " +
            "c.`name` as category, " +
            " ifnull(g.name_vi ,bet.game_name)  as  game_name, " +
            " ifnull(g.name_en ,bet.game_name)  as game_name_EN, " +
            "bet.bet_amount as betAmount, " +
            "bet.settle as settle, " +
            "(bet.win_loss*-1)  as winLoss, " +
            "bet.valid_bet_amount as valid_bet_amount, " +
            "'' as detail, " +
            "bet.bet_state as bet_state, " +
            "bet.settle_note as settle_note " +
            "FROM " +
            "ga_bet_order_${tableId} bet " +
            "JOIN ga_platform p ON p.id = bet.platform_id " +
            "JOIN ga_game_category c ON c.CODE = bet.game_category_code " +
            "left JOIN ga_platform_game g ON g.game_id = bet.game_id and g.platform_id =bet.platform_id  " +
            " ${ew.customSqlSegment}")
    Page<BetOrderDetailRecordVO> getBetOrderRecordDetail(Page<BetOrderDetailRecordVO> page, @Param("tableId") int tableId, @Param(Constants.WRAPPER) QueryWrapper<BetOrderDetailRecordVO> queryWrapper);

    @Select("SELECT " +
            "ROW_NUMBER() OVER (" +
            "ORDER BY member_id ASC) as id ," +
            "member_id," +
            "platform_id," +
            "c.name as platformCode," +
            "settle_day as settle_time," +
            "game_category_code," +
            "bet_amount," +
            "valid_bet_amount," +
            "settle," +
            "(win_loss * -1) as win_loss " +
            "from " +
            "(" +
            "SELECT " +
            "member_id ," +
            "platform_id," +
            "game_category_code," +
            "SUM(bet_amount)as bet_amount ," +
            "sum(valid_bet_amount)as valid_bet_amount ," +
            "sum(settle) as settle ," +
            "sum(win_loss) as win_loss," +
            "date_format(transaction_time, '%Y-%m-%d') as settle_day " +
            "from " +
            "ga_bet_order ${ew.customSqlSegment}" +
            "group by " +
            "member_id ," +
            "platform_id," +
            "game_category_code," +
            "settle_day ) a " +
            "left join ga_platform c on " +
            "c.id = a.platform_id " +
            "order by " +
            "settle_time desc")
    Page<MemberBetAmountRecordDailyVO> findDailyBetAmount(Page<MemberBetAmountRecordDaily> page, @Param(Constants.WRAPPER) LambdaQueryWrapper<MemberBetAmountRecordDaily> param);

    @Select("SELECT " +
            "IFNULL(a.bet_times,0) as bet_times," +
            "username," +
            "member_id," +
            "platform_id," +
            "c.name as platform_code," +
            "settle_day as settle_time," +
            "game_category_code," +
            "IFNULL(bet_amount,0) as bet_amount," +
            "IFNULL(valid_bet_amount,0) as valid_bet_amount," +
            "IFNULL(settle,0) as settle," +
            "IFNULL((win_loss * -1),0) as win_loss " +
            "from " +
            "(" +
            "SELECT " +
            "count(1) as bet_times," +
            "username," +
            "member_id ," +
            "platform_id," +
            "game_category_code," +
            "SUM(bet_amount)as bet_amount ," +
            "sum(valid_bet_amount)as valid_bet_amount ," +
            "sum(settle) as settle ," +
            "sum(win_loss) as win_loss," +
            "date_format(settle_time, '%Y-%m-%d') as settle_day " +
            "from " +
            "ga_bet_order_${tableId} ${ew.customSqlSegment}" +
            "group by " +
            "member_id ," +
            "platform_id," +
            "game_category_code," +
            "settle_day ) a " +
            "left join ga_platform c on " +
            "c.id = a.platform_id " +
            "order by " +
            "settle_time desc")
    List<MemberBetAmountRecordDailyVO> findDailyBetAmountForRebate(@Param("tableId") int tableId, @Param(Constants.WRAPPER) LambdaQueryWrapper<MemberBetAmountRecordDaily> param);

    Page<H5MemberAccountBetDetailRecordVO> findMemberAccountBetRecordPlatformDate(Page<Object> page, Long memberId, LocalDateTime startTime, LocalDateTime endTime, FindMemberAccountBetRecordForm form);

}
