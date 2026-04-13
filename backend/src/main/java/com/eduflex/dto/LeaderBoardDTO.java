package com.eduflex.dto;

import java.util.List;
import java.util.UUID;

public class LeaderBoardDTO {
  public record LeaderBoardUserInfo(UUID userID, String fullName, int xp, int level) {
  }

  public record LeaderBoardItem(int rank, UUID userID, String fullName, int xp, int level) {
  }

  public record GetLeaderBoardResponse(boolean success, String message, List<LeaderBoardItem> leaderBoard) {
  }
}
