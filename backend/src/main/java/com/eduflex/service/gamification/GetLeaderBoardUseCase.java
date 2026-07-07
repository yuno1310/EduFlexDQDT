package com.eduflex.service.gamification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.eduflex.dto.gamification.LeaderBoardDTO.GetLeaderBoardResponse;
import com.eduflex.dto.gamification.LeaderBoardDTO.LeaderBoardItem;
import com.eduflex.dto.gamification.LeaderBoardDTO.LeaderBoardUserInfo;
import com.eduflex.repository.gamification.GamificationStatsRepository;

@Service
public class GetLeaderBoardUseCase {

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;


    private static final String LEADERBOARD_KEY = "eduflex:leaderboard:xp";

    public GetLeaderBoardResponse execute(int top) {
        List<LeaderBoardItem> list = new ArrayList<>();

        Set<ZSetOperations.TypedTuple<String>> redisTopUsers = 
            redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, top - 1);

        if (redisTopUsers != null && !redisTopUsers.isEmpty()) {
            int currentRank = 1;
            
            for (ZSetOperations.TypedTuple<String> tuple : redisTopUsers) {
                UUID userId = UUID.fromString(tuple.getValue());
                int xp = tuple.getScore().intValue(); // ZSET lưu score dưới dạng double
                LeaderBoardUserInfo userInfo = gamificationStatsRepository.getUserInfoById(userId); 

                if (userInfo != null) {
                    list.add(new LeaderBoardItem(currentRank, userId, userInfo.fullName(), xp, userInfo.level()));
                    currentRank++;
                }
            }
            return new GetLeaderBoardResponse(true, "Get Leader Board from Redis successfully", list);
        }


        List<LeaderBoardUserInfo> dbUsers = gamificationStatsRepository.getLeaderBoard(top);

        if (dbUsers == null || dbUsers.isEmpty()) {
            return new GetLeaderBoardResponse(false, "Leader Board is empty", list);
        }

        int currentRank = 1;
        for (LeaderBoardUserInfo user : dbUsers) {
            list.add(new LeaderBoardItem(currentRank, user.userID(), user.fullName(), user.xp(), user.level()));
            
            redisTemplate.opsForZSet().add(LEADERBOARD_KEY, String.valueOf(user.userID()), user.xp());
            
            currentRank++;
        }

        return new GetLeaderBoardResponse(true, "Get Leader Board from DB successfully", list);
    }
}