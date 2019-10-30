package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
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

    // lista de partidas
    @RequestMapping("/games")
    public Map<String, Object> getGames(Authentication authentication) {
        Map<String, Object> data = new LinkedHashMap<>();
        List<Game> game = gameRepository.findAll();

        if (this.isGuest(authentication)) {
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
        if (this.isGuest(authentication)) {
            return this.createEntityResponse("error", "sin autorizacion", HttpStatus.UNAUTHORIZED);
        }

        // crear el juego
        Game nuevo_juego = new Game();
        gameRepository.save(nuevo_juego);

        // player logueado
        Player current_player = playerRepository.findByUserName(authentication.getName());

        // nuevo game player
        GamePlayer nuevo_game_player = new GamePlayer(nuevo_juego, current_player, new Date());
        GamePlayer guardado = gamePlayerRepository.save(nuevo_game_player);

        // respuesta
        Map<String, Object> mapa = new HashMap<String, Object>();
        mapa.put("gpid", guardado.getId());

        return this.createEntityResponse(mapa, HttpStatus.CREATED);
    }

    // unirse a un juego
    @RequestMapping(value = "/game/{game_id}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long game_id, Authentication authentication) {
        // sin login
        if (this.isGuest(authentication)) {
            return this.createEntityResponse("error", "sin autorizacion", HttpStatus.UNAUTHORIZED);
        }

        Player current_player = playerRepository.findByUserName(authentication.getName());

        // con id de game vacio
        if (game_id.equals(null)) {
            return this.createEntityResponse("error", "No existe el juego", HttpStatus.FORBIDDEN);
        }

        // juego con un solo jugador
        Game juego = gameRepository.findById(game_id).orElse(null);
        if (juego == null) {
            return this.createEntityResponse("error", "No existe el juego", HttpStatus.FORBIDDEN);
        }

        // si el juego no tiene un solo player
        if (juego.getGamePlayers().size() != 1) {
            return this.createEntityResponse("error", "El juego está lleno", HttpStatus.FORBIDDEN);
        }

        /**
         * crear y guardar el game player
         */
        GamePlayer nuevo_game_player = new GamePlayer(juego, current_player, new Date());
        GamePlayer game_player_guardado = gamePlayerRepository.save(nuevo_game_player);

        Map<String, Object> mapa = new HashMap<String, Object>();
        mapa.put("gpid", game_player_guardado.getId());

        return this.createEntityResponse(mapa, HttpStatus.CREATED);
    }

    // guardar posiciones de ships
    @RequestMapping(value = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> placeShip(@PathVariable Long gamePlayerId, @RequestBody List<Ship> ships,
                                                         Authentication authentication) {
        // no está logueado
        if (this.isGuest(authentication)) {
            return this.createEntityResponse("error", "no hay usuario", HttpStatus.UNAUTHORIZED);
        }

        // player logueado
        Player current_player = playerRepository.findByUserName(authentication.getName());

        // no existe el gamePlayer id
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (gamePlayer == null) {
            return this.createEntityResponse("error", "no existe la partida", HttpStatus.UNAUTHORIZED);
        }

        // el usuario logueado no es del juego
        if (gamePlayer.getPlayer().getId() != current_player.getId()) {
            return this.createEntityResponse("error", "su usuario no tiene permiso", HttpStatus.UNAUTHORIZED);
        }

        // el player ya tiene ships
        if (gamePlayer.getShips().isEmpty()) {
            return this.createEntityResponse("error", "su usuario ya tiene barcos", HttpStatus.FORBIDDEN);
        }

        /**
         * guardar los ships y sus ubicaciones
         */
        ships.forEach(ship -> ship.setGamePlayer(gamePlayer));
        ships.forEach(shipR -> shipRepository.save(shipR));
        return createEntityResponse("OK", "ubicaciones guardadas", HttpStatus.CREATED);
    }


    // guardar salvos
    @RequestMapping(value = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> storeSalvoes(@PathVariable Long gamePlayerId, @RequestBody Salvo salvo, Authentication authentication) {
        // no está logueado
        if (this.isGuest(authentication)) {
            return this.createEntityResponse("error", "no hay usuario", HttpStatus.UNAUTHORIZED);
        }

        // player logueado
        Player current_player = playerRepository.findByUserName(authentication.getName());

        // no existe el gamePlayer id
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (gamePlayer == null) {
            return this.createEntityResponse("error", "no existe la partida", HttpStatus.UNAUTHORIZED);
        }

        // el usuario logueado no es del juego
        if (gamePlayer.getPlayer().getId() != current_player.getId()) {
            return this.createEntityResponse("error", "su usuario no tiene permiso", HttpStatus.UNAUTHORIZED);
        }

        // setear turno
        if (gamePlayer.getSalvoes().isEmpty()) {
            salvo.setTurn(1);
        } else {
            salvo.setTurn(gamePlayer.getSalvoes().size() + 1);
        }


        // no repetir turno
        Long existe_turno = gamePlayer.getSalvoes().stream()
                .filter(cada_salvo -> cada_salvo.getTurn() == salvo.getTurn()).count();

        if (existe_turno > 0) {
            return this.createEntityResponse("error", "ya se envio el turno", HttpStatus.FORBIDDEN);
        }

        /**
         * guardar salvo
         */
        salvo.setGamePlayer(gamePlayer);
        salvoRepository.save(salvo);
        return this.createEntityResponse("OK", "guardado", HttpStatus.OK);
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
