package com.codeoftheweb.salvo.repositories;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.models.Salvo;
import com.codeoftheweb.salvo.models.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Score findByPlayerAndGame(Player player, Game game);
}
