package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
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
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    GameService gameService;

    // lista de partidas
    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<Game> game = gameRepository.findAll();

        if (gameService.isGuest(authentication)) {
            data.put("player", "Guest");
        } else {
            Player get_current_player = playerRepository.findByUserName(authentication.getName());
            data.put("player", get_current_player.makePlayerDTO());
        }
        data.put("games", game.stream().map(game1 -> game1.makeGameDTO()));

        return data;
    }

    @RequestMapping(value = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        if (gameService.isGuest(authentication)) {
            return gameService.createEntityResponse("error", "Not authorized", HttpStatus.UNAUTHORIZED);
        }

        // crear el juego
        Game new_game = new Game();
        gameRepository.save(new_game);

        // player logueado
        Player current_player = playerRepository.findByUserName(authentication.getName());

        // nuevo game player
        GamePlayer new_game_player = new GamePlayer(new_game, current_player, new Date());
        GamePlayer game_player_saved = gamePlayerRepository.save(new_game_player);

        // respuesta
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("gpid", game_player_saved.getId());

        return gameService.createEntityResponse(map, HttpStatus.CREATED);
    }

    // unirse a un juego
    @RequestMapping(value = "/game/{game_id}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long game_id, Authentication authentication) {
        // sin login
        if (gameService.isGuest(authentication)) {
            return gameService.createEntityResponse("error", "Not authorized", HttpStatus.UNAUTHORIZED);
        }

        Player current_player = playerRepository.findByUserName(authentication.getName());

        // con id de game vacio
        if (game_id.equals(null)) {
            return gameService.createEntityResponse("error", "Game not found", HttpStatus.FORBIDDEN);
        }

        // juego con un solo jugador
        Game game = gameRepository.findById(game_id).orElse(null);
        if (game == null) {
            return gameService.createEntityResponse("error", "Game not found", HttpStatus.FORBIDDEN);
        }

        // si el juego no tiene un solo player
        if (game.getGamePlayers().size() != 1) {
            return gameService.createEntityResponse("error", "Game full", HttpStatus.FORBIDDEN);
        }

        /**
         * crear y guardar el game player
         */
        GamePlayer new_game_player = new GamePlayer(game, current_player, new Date());
        GamePlayer game_player_saved = gamePlayerRepository.save(new_game_player);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("gpid", game_player_saved.getId());

        return gameService.createEntityResponse(map, HttpStatus.CREATED);
    }

    // guardar posiciones de ships
    @RequestMapping(value = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placeShip(@PathVariable Long gamePlayerId, @RequestBody List<Ship> ships,
                                                         Authentication authentication) {
        // no está logueado
        if (gameService.isGuest(authentication)) {
            return gameService.createEntityResponse("error", "User not found", HttpStatus.UNAUTHORIZED);
        }

        // player logueado
        Player current_player = playerRepository.findByUserName(authentication.getName());

        // no existe el gamePlayer id
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (gamePlayer == null) {
            return gameService.createEntityResponse("error", "Match not found", HttpStatus.UNAUTHORIZED);
        }

        // el usuario logueado no es del juego
        if (gamePlayer.getPlayer().getId() != current_player.getId()) {
            return gameService.createEntityResponse("error", "User does not have permission", HttpStatus.UNAUTHORIZED);
        }

        // el player ya tiene ships
        if (!gamePlayer.getShips().isEmpty()) {
            return gameService.createEntityResponse("error", "You already has ships", HttpStatus.FORBIDDEN);
        }

        /**
         * guardar los ships y sus ubicaciones
         */
        ships.forEach(ship -> ship.setGamePlayer(gamePlayer));
        ships.forEach(shipR -> shipRepository.save(shipR));
        return gameService.createEntityResponse("OK", "Saved locations", HttpStatus.CREATED);
    }


    // guardar salvos
    @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> storeSalvoes(@PathVariable Long gamePlayerId, @RequestBody Salvo salvo,
                                                            Authentication authentication) {
        // no está logueado
        if (gameService.isGuest(authentication)) {
            return gameService.createEntityResponse("error", "User not found", HttpStatus.UNAUTHORIZED);
        }

        // player logueado
        Player current_player = playerRepository.findByUserName(authentication.getName());

        // no existe el gamePlayer id
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (gamePlayer == null) {
            return gameService.createEntityResponse("error", "Match not found", HttpStatus.UNAUTHORIZED);
        }

        // el usuario logueado no es del juego
        if (gamePlayer.getPlayer().getId() != current_player.getId()) {
            return gameService.createEntityResponse("error", "User does not have permission", HttpStatus.UNAUTHORIZED);
        }

        // setear turno
        if (gamePlayer.getSalvoes().isEmpty()) {
            salvo.setTurn(1);
        } else {
            salvo.setTurn(gamePlayer.getSalvoes().size() + 1);
        }

        // no repetir turno
        Long exists_turn = gamePlayer.getSalvoes().stream()
                .filter(each_salvo -> each_salvo.getTurn() == salvo.getTurn()).count();

        if (exists_turn > 0) {
            return gameService.createEntityResponse("error", "The turn already exists", HttpStatus.FORBIDDEN);
        }

        // evitar enviar dos salvos seguidos
        int max_salvo_current = gamePlayer.getSalvoes().size();
        GamePlayer opponent_game_player = gamePlayer.getGame()
                .getGamePlayers()
                .stream()
                .filter(gamePlayer1 -> gamePlayer1.getId() != gamePlayerId)
                .findAny()
                .orElse(null);

        if (opponent_game_player != null) {
            int max_salvo_opponent = opponent_game_player.getSalvoes().size();

            if ((max_salvo_current - max_salvo_opponent) == 1) {
                return gameService.createEntityResponse("error", "You cannot add consecutive salvoes", HttpStatus.FORBIDDEN);
            }
        }

        // no enviar salvoes sin ya terminó el juego
        List<String> game_over_states = new ArrayList<String>();
        game_over_states.add("WON");
        game_over_states.add("LOST");
        game_over_states.add("TIE");

        String actual_state = gameService.makeStateDTO(gamePlayer);

        if (game_over_states.contains(actual_state)) {
            return gameService.createEntityResponse("error", "Game over", HttpStatus.FORBIDDEN);
        }

        /**
         * guardar salvo
         */
        salvo.setGamePlayer(gamePlayer);
        salvoRepository.save(salvo);
        return gameService.createEntityResponse("OK", "Saved", HttpStatus.OK);
    }
}
