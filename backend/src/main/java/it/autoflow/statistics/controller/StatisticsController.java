package it.autoflow.statistics.controller;

import it.autoflow.statistics.dto.DashboardStatisticsDTO;
import it.autoflow.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatisticsDTO> getDashboard() {
        DashboardStatisticsDTO dto = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(dto);
    }
}