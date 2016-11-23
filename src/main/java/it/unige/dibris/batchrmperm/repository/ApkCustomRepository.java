package it.unige.dibris.batchrmperm.repository;

import it.unige.dibris.batchrmperm.domain.ApkCustom;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApkCustomRepository extends CrudRepository<ApkCustom, Long> {

}
