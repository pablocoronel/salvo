package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
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

    // partida de cada jugador
    @RequestMapping("game_view/{gp}")
    public ResponseEntity<Map<String, Object>> getGames(@PathVariable Long gp, Authentication authentication) {
        GamePlayer game_player = gamePlayerRepository.findById(gp).get();
        Game game = game_player.getGame();
        Player current_user = playerRepository.findByUserName(authentication.getName());

        // No ver el juego de otro player
        if (game_player.getPlayer().getId() != current_user.getId()) {
            return this.createEntityResponse("error", "no autorizado", HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> data = new LinkedHashMap<String, Object>();

        data.put("id", game.getId());
        data.put("created", game.getCreationDate());
        data.put("gameState", "PLACESHIPS");
        data.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO()));
        data.put("ships", game_player.getShips().stream().map(ship -> ship.makeShipDTO()));

        data.put("salvoes", game.getGamePlayers().stream().map(gamePlayer -> gamePlayer.getSalvoes())
                .flatMap(salvos -> salvos.stream()).map(s -> s.makeSalvoDTO()));

        // hits hardcodeados
        Map<String, Object> mapa_hits = new HashMap<String, Object>();
        List<String> lista_vacia = new ArrayList<String>();
        mapa_hits.put("self", lista_vacia);
        mapa_hits.put("opponent", lista_vacia);
        data.put("hits", mapa_hits);

        return this.createEntityResponse(data, HttpStatus.OK);
    }

    // crear un player
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String email, @RequestParam String password) {
        ResponseEntity<Map<String, Object>> respuesta = null;

        // validar data ingresada
        if (email.isEmpty() || password.isEmpty()) {
            respuesta = this.createEntityResponse("error", "email o password vacio", HttpStatus.FORBIDDEN);
        }

        // consultar si ya existe un usuario igual
        Player player = playerRepository.findByUserName(email);
        if (player != null) {
            respuesta = this.createEntityResponse("error", "ya existe el usuario", HttpStatus.CONFLICT);
        } else {
            Player nuevo = new Player();
            nuevo.setUserName(email);
            nuevo.setPassword(passwordEncoder.encode(password));

            playerRepository.save(nuevo);

            respuesta = this.createEntityResponse("success", "usuario creado", HttpStatus.CREATED);
        }

        return respuesta;
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
        Game juego = gameRepository.findById(game_id).get();
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
