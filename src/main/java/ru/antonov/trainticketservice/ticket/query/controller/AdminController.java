package ru.antonov.trainticketservice.ticket.query.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.antonov.trainticketservice.ticket.query.service.ReadModelRestoreService;

/**
 * REST-контроллер query-side части  для администратора.
 */
@RestController
@RequestMapping("/query/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminController {
    private final ReadModelRestoreService readModelRestoreService;

    /**
     * Воcстанавливает read-model из событий в event_store
     */
    @PostMapping("/restore")
    public ResponseEntity<Void> restore() {
        readModelRestoreService.restore();

        return ResponseEntity.ok().build();
    }
}
