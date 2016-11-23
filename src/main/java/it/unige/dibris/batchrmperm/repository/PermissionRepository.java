package it.unige.dibris.batchrmperm.repository;


import it.unige.dibris.batchrmperm.domain.Permission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends CrudRepository<Permission, String> {

}
