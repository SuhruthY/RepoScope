package com.suhruth.reposcope.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suhruth.reposcope.service.RepositoryAnalysisService;

@RestController
@RequestMapping("/api/repo")
public class RepositoryAnalysisController {

    @Autowired
    private RepositoryAnalysisService service;

    @GetMapping("/analyze")
    public Map<String, Object> analyzeRepository(@RequestParam String repoUrl) {
        return service.analyzeRepository(repoUrl);
    }
}
