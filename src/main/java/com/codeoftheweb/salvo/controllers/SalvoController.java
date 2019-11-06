package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import com.codeoftheweb.salvo.repositories.ScoreRepository;
import com.codeoftheweb.salvo.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private GameService gameService;

    // partida de cada jugador
    @RequestMapping("game_view/{gp}")
    public ResponseEntity<Map<String, Object>> getGames(@PathVariable Long gp, Authentication authentication) {
        GamePlayer game_player = gamePlayerRepository.findById(gp).orElse(null);

        if (game_player == null) {
            return gameService.createEntityResponse("error", "Not found", HttpStatus.UNAUTHORIZED);
        }

        Game game = game_player.getGame();
        Player current_user = playerRepository.findByUserName(authentication.getName());

        // No ver el juego de otro player
        if (game_player.getPlayer().getId() != current_user.getId()) {
            return gameService.createEntityResponse("error", "Not authorized", HttpStatus.UNAUTHORIZED);
        }

        // gameplayers de self y oponente
        /**
         * game player self
         */
        GamePlayer game_player_self = game_player;

        long id_juego = game.getId();

        /**
         * game player opponent
         */
        GamePlayer game_player_opponent = game.getGamePlayers().stream()
                .filter(gamePlayer -> gamePlayer.getId() != gp)
                .findFirst().orElse(null);

        // data
        Map<String, Object> data = new LinkedHashMap<String, Object>();

        data.put("id", game.getId());
        data.put("created", game.getCreationDate());
        data.put("gameState", gameService.makeStateDTO(game_player_self));
        data.put("gamePlayers", game.getGamePlayers()
                .stream()
                .map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO()));
        data.put("ships", game_player.getShips()
                .stream()
                .map(ship -> ship.makeShipDTO()));
        data.put("salvoes", game.getGamePlayers()
                .stream()
                .map(gamePlayer -> gamePlayer.getSalvoes())
                .flatMap(salvos -> salvos.stream())
                .map(s -> s.makeSalvoDTO()));
        data.put("hits", gameService.makeHitsDTO(game_player_self, game_player_opponent));

        return gameService.createEntityResponse(data, HttpStatus.OK);
    }
}
