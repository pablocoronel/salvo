package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import com.codeoftheweb.salvo.repositories.ScoreRepository;
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
    private ScoreRepository scoreRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    // partida de cada jugador
    @RequestMapping("game_view/{gp}")
    public ResponseEntity<Map<String, Object>> getGames(@PathVariable Long gp, Authentication authentication) {
        GamePlayer game_player = gamePlayerRepository.findById(gp).orElse(null);

        if (game_player == null) {
            return this.createEntityResponse("error", "no encontrado", HttpStatus.UNAUTHORIZED);
        }

        Game game = game_player.getGame();
        Player current_user = playerRepository.findByUserName(authentication.getName());

        // No ver el juego de otro player
        if (game_player.getPlayer().getId() != current_user.getId()) {
            return this.createEntityResponse("error", "no autorizado", HttpStatus.UNAUTHORIZED);
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
        data.put("gameState", this.makeStateDTO(game_player_self));
        data.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO()));
        data.put("ships", game_player.getShips().stream().map(ship -> ship.makeShipDTO()));
        data.put("salvoes", game.getGamePlayers().stream().map(gamePlayer -> gamePlayer.getSalvoes())
                .flatMap(salvos -> salvos.stream()).map(s -> s.makeSalvoDTO()));
        data.put("hits", game.makeHitsDTO(game_player_self, game_player_opponent));

        return this.createEntityResponse(data, HttpStatus.OK);
    }

    // estado del juego
    private String makeStateDTO(GamePlayer game_player_self) {
        String state = "PLACESHIPS";

        // WAITINGFOROPP
        GamePlayer opponent_game_player = game_player_self.getGame().getGamePlayers()
                .stream()
                .filter(gamePlayer -> gamePlayer.getId() != game_player_self.getId())
                .findFirst()
                .orElse(null);

        if (game_player_self.getShips().isEmpty()) {
            state = "PLACESHIPS";
        } else if (opponent_game_player == null) {
            state = "WAITINGFOROPP";
        } else if (opponent_game_player.getShips().isEmpty()) {
            state = "WAIT";
        } else if (game_player_self.getSalvoes().size() > opponent_game_player.getSalvoes().size()) {
            state = "WAIT";
        } else {
            state = "PLAY";
        }

        // self
        String jugando_self = this.getStateGame(game_player_self, opponent_game_player);
        String jugando_opponent = this.getStateGame(opponent_game_player, game_player_self);

        if (jugando_self == "LOST" && jugando_opponent == "PLAY") {
            state = "LOST";

            // guardar el score
            this.storeScore(game_player_self.getPlayer(), game_player_self.getGame(), 0.0);
        } else if (jugando_self == "PLAY" && jugando_opponent == "LOST") {
            state = "WON";

            // guardar el score
            this.storeScore(game_player_self.getPlayer(), game_player_self.getGame(), 1.0);
        } else if (jugando_self == "LOST" && jugando_opponent == "LOST") {
            state = "TIE";

            // guardar el score
            this.storeScore(game_player_self.getPlayer(), game_player_self.getGame(), 0.5);
        }

        return state;
    }

    private String getStateGame(GamePlayer self_game_player, GamePlayer opponent_game_player) {
        String state = "PLAY";

        if (opponent_game_player != null && self_game_player != null) {
            int cantidad_ships = self_game_player.getShips().size();
            int contador_ships_hundidos = 0;

            Map<String, Object> hits = self_game_player.getGame().makeHitsDTO(self_game_player, opponent_game_player);

            List hit = (List) hits.getOrDefault("self", null);
            if (hit.size() > 0) {
                Map mapa_damages = (Map) hit.get(hit.size() - 1);
                Map damages = (Map) mapa_damages.get("damages");

                for (Ship ship : self_game_player.getShips()) {
                    String key = ship.getType().toLowerCase().replace(" ", "");
                    Long a = (Long) damages.get(key);

                    if (ship.getLocations().size() == a) {
                        contador_ships_hundidos++;
                    }
                }

                if (cantidad_ships == contador_ships_hundidos) {
                    state = "LOST";
                }
            }
        }

        return state;
    }

    private void storeScore(Player player, Game game, double score) {
        Date date = new Date();

        Score score_1 = new Score(player, game, date, score);
        scoreRepository.save(score_1);
    }



    // saber si un usuario está logueado
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    // hacer un ResponseEntity de map
    private ResponseEntity<Map<String, Object>> createEntityResponse(String clave, String mensaje, HttpStatus httpStatus) {
        Map<String, Object> mapa = new HashMap<String, Object>();

        mapa.put(clave, mensaje);
        return new ResponseEntity<Map<String, Object>>(mapa, httpStatus);
    }

    private ResponseEntity<Map<String, Object>> createEntityResponse(Map mapa, HttpStatus httpStatus) {
        return new ResponseEntity<Map<String, Object>>(mapa, httpStatus);
    }
}
