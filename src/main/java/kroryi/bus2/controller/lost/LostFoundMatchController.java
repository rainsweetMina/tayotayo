package kroryi.bus2.controller.lost;

import kroryi.bus2.dto.lost.LostFoundMatchRequestDTO;
import kroryi.bus2.entity.LostFoundMatch;
import kroryi.bus2.service.LostFoundMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class LostFoundMatchController {

    private final LostFoundMatchService matchService;

    @PostMapping
    public ResponseEntity<LostFoundMatch> matchLostAndFound(@RequestBody LostFoundMatchRequestDTO dto) {
        LostFoundMatch saved = matchService.matchItems(dto);
        return ResponseEntity.ok(saved);
    }
}

