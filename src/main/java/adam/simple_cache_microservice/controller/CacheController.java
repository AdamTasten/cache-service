package adam.simple_cache_microservice.controller;

import adam.simple_cache_microservice.dto.CacheEntryDto;
import adam.simple_cache_microservice.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/")
class CacheController {

    @Autowired
    private CacheService cacheService;

    @PostMapping
    public ResponseEntity<Void> put(@RequestBody CacheEntryDto cacheEntryDto) {
        cacheService.save(cacheEntryDto.getKey(), cacheEntryDto.getValue());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> get(@PathVariable String key) {
        Optional<String> value = cacheService.findByKey(key);
        return value.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}