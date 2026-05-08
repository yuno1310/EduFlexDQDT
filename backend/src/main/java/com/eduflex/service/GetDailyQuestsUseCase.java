package com.eduflex.service;

import com.eduflex.dto.DailyQuestDTO.DailyQuestResponse;
import com.eduflex.repository.DailyQuestRepository;
import com.eduflex.repository.DailyQuestRepository.ProgressRow;
import com.eduflex.repository.DailyQuestRepository.QuestRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GetDailyQuestsUseCase {

    @Autowired
    private DailyQuestRepository dailyQuestRepository;

    public List<DailyQuestResponse> execute(UUID userId) {
        LocalDate today = LocalDate.now();
        List<QuestRow> quests = dailyQuestRepository.findAllQuests();
        List<DailyQuestResponse> result = new ArrayList<>();

        for (QuestRow quest : quests) {
            ProgressRow progress = dailyQuestRepository.findProgress(userId, quest.id(), today);
            int currentCount = progress != null ? progress.currentCount() : 0;
            boolean completed = progress != null && progress.completed();
            result.add(new DailyQuestResponse(
                quest.id(), quest.title(), quest.description(),
                quest.questType(), quest.targetCount(), quest.xpReward(),
                currentCount, completed
            ));
        }
        return result;
    }
}
