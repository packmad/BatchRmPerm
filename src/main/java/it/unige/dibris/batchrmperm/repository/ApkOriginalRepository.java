package it.unige.dibris.batchrmperm.repository;

import it.unige.dibris.batchrmperm.domain.ApkOriginal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApkOriginalRepository extends CrudRepository<ApkOriginal, Long> {

}