package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.models.Ship;
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
