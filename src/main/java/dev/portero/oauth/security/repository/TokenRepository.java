package dev.portero.oauth.security.repository;

import dev.portero.oauth.security.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query(value = """
            SELECT token FROM Token token INNER JOIN Client client\s
            ON token.client.id = client.id\s
            WHERE client.id = :id AND (token.expired = FALSE OR token.revoked = FALSE)\s
            """)
    List<Token> findAllValidTokenByClientId(Long id);

    Optional<Token> findByToken(String authToken);
}
