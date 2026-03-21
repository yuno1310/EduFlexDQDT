package com.eduflex.service;

import com.eduflex.dto.GetAllBadgesDTO;
import com.eduflex.repository.BadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllBadgesUseCase {

    @Autowired
    private BadgeRepository badgeRepository;

    public List<GetAllBadgesDTO.GetAllBadgesResponse> execute() {
        return badgeRepository.findAll().stream()
                .map(b -> new GetAllBadgesDTO.GetAllBadgesResponse(
                        b.record.getId(),
                        b.record.getName(),
                        b.record.getDescription(),
                        b.record.getIconUrl(),
                        b.record.getConditionType()
                ))
                .toList();
    }
}
