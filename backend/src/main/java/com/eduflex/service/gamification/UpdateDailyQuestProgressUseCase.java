package com.eduflex.service.gamification;

import com.eduflex.dto.gamification.AddXpDTO;
import com.eduflex.dto.quiz.QuizDTO.CompletedQuestInfo;
import com.eduflex.repository.gamification.DailyQuestRepository;
import com.eduflex.repository.gamification.DailyQuestRepository.ProgressRow;
import com.eduflex.repository.gamification.DailyQuestRepository.QuestRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UpdateDailyQuestProgressUseCase {

    @Autowired
    private DailyQuestRepository dailyQuestRepository;

    @Autowired
    private AddXpUseCase addXpUseCase;

    @Transactional
    public CompletedQuestInfo execute(UUID userId, String questType, int increment) {
        Long questId = dailyQuestRepository.findQuestIdByType(questType);
        if (questId == null) return null;

        LocalDate today = LocalDate.now();
        ProgressRow current = dailyQuestRepository.findProgress(userId, questId, today);

        // Already completed today — no-op
        if (current != null && current.completed()) return null;

        boolean reset = increment < 0;
        dailyQuestRepository.upsertProgress(userId, questId, today, Math.max(increment, 0), reset);

        if (reset) return null;

        // Re-fetch to get updated count
        ProgressRow updated = dailyQuestRepository.findProgress(userId, questId, today);
        if (updated == null) return null;

        List<QuestRow> quests = dailyQuestRepository.findAllQuests();
        QuestRow quest = quests.stream().filter(q -> q.id().equals(questId)).findFirst().orElse(null);
        if (quest == null) return null;

        if (updated.currentCount() >= quest.targetCount()) {
            dailyQuestRepository.markCompleted(userId, questId, today);
            addXpUseCase.execute(userId, new AddXpDTO.AddXpRequest(quest.xpReward()));
            return new CompletedQuestInfo(quest.title(), quest.xpReward());
        }
        return null;
    }
}
