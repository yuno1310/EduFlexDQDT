package com.eduflex.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.LeaderBoardDTO.GetLeaderBoardResponse;
import com.eduflex.dto.LeaderBoardDTO.LeaderBoardItem;
import com.eduflex.dto.LeaderBoardDTO.LeaderBoardUserInfo;
import com.eduflex.repository.GamificationStatsRepository;

@Service
public class GetLeaderBoardUseCase {
  @Autowired
  private GamificationStatsRepository gamificationStatsRepository;

  public GetLeaderBoardResponse execute(int top) {
    List<LeaderBoardUserInfo> users = gamificationStatsRepository.getLeaderBoard(top);
    List<LeaderBoardItem> list = new ArrayList<LeaderBoardItem>();

    if (users == null || users.isEmpty()) {
      return new GetLeaderBoardResponse(false, "Leader Board is empty", list);
    }

    int currentRank = 1;

    for (LeaderBoardUserInfo user : users) {
      list.add(new LeaderBoardItem(currentRank, user.userID(), user.fullName(), user.xp(), user.level()));
      ++currentRank;
    }
    return new GetLeaderBoardResponse(true, "Get Leader Board successfully", list);
  }
}
