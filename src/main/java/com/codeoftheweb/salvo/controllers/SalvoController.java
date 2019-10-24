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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    // partida de cada jugador
    @RequestMapping("game_view/{nn}")
    public Map<String, Object> getGames(@PathVariable Long nn) {
        GamePlayer game_player = gamePlayerRepository.findById(nn).get();
        Game game = game_player.getGame();

        Map<String, Object> data = new LinkedHashMap<String, Object>();

        data.put("id", game.getId());
        data.put("created", game.getCreationDate());
        data.put("gamePlayers", game.getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO()));
        data.put("ships", game_player.getShips().stream().map(ship -> ship.makeShipDTO()));

        data.put("salvoes", game.getGamePlayers().stream().map(gamePlayer -> gamePlayer.getSalvoes())
                .flatMap(salvos -> salvos.stream()).map(s -> s.makeSalvoDTO()));

        return data;
    }

    // crear un player
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String email, @RequestParam String password){
        ResponseEntity<Map<String, Object>> respuesta = null;

        // validar data ingresada
        if (email.isEmpty() || password.isEmpty()){
            respuesta = this.createEntityResponse("error", "email o password vacio", HttpStatus.FORBIDDEN);
        }

        // consultar si ya existe un usuario igual
        Player player = playerRepository.findByUserName(email);
        if(player != null){
            respuesta = this.createEntityResponse("error", "ya existe el usuario", HttpStatus.CONFLICT);
        }else {
            Player nuevo = new Player();
            nuevo.setUserName(email);
            nuevo.setPassword(passwordEncoder.encode(password));

            playerRepository.save(nuevo);

            respuesta = this.createEntityResponse("success", "usuario creado", HttpStatus.CREATED);
        }

        return respuesta;
    }

    // saber si un usuario está logueado
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    // hacer un ResponseEntity de map
    private ResponseEntity<Map<String, Object>> createEntityResponse(String clave, String mensaje, HttpStatus httpStatus){
        Map<String, Object> mapa = new HashMap<>();

        mapa.put(clave, mensaje);
        return new ResponseEntity<Map<String, Object>>(mapa, httpStatus);
    }
}
