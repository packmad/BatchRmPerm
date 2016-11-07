package it.unige.dibris.batchrmperm.repository;


import it.unige.dibris.batchrmperm.domain.Apk;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApkRepository extends CrudRepository<Apk, Long> {
    Apk findBySha256Hash(String sha256Hash);
}