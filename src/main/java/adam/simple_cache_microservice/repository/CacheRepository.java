package adam.simple_cache_microservice.repository;

import adam.simple_cache_microservice.model.CacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CacheRepository extends JpaRepository<CacheEntity, String> {
}
