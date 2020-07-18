package br.com.economizenergiasecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.economizenergiasecurity.domain.Authority;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
