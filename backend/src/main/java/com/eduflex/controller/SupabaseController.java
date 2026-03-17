package com.eduflex.controller;

import com.eduflex.service.SupabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/supabase")
public class SupabaseController {

    private final SupabaseService supabaseService;

    public SupabaseController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkSupabaseHealth() {
        return ResponseEntity.ok(supabaseService.checkConnection());
    }
}
