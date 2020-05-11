package br.com.una.easygamesecurity.repository;

import br.com.una.easygamesecurity.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
