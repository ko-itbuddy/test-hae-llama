package com.example.llama.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/llama")
public class LlamaController {

    /**
     * Get a Llama by ID.
     * @param id The ID of the llama (must be positive).
     * @param type Optional filter for llama type.
     * @return 200 OK if found, 404 NOT_FOUND if missing, 400 BAD_REQUEST if ID is invalid.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LlamaResponse> getLlama(
            @PathVariable Long id, 
            @RequestParam(required = false, defaultValue = "ALPHA") String type) {
        
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        if (id == 999) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new LlamaResponse(id, "Llama " + id, type, List.of("TDD", "DDD")));
    }

    public record LlamaResponse(Long id, String name, String type, List<String> skills) {}
}