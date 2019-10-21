package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // lista de partidas
    @RequestMapping("/games")
    public Map<String, Object> getGames() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<Game> game = gameRepository.findAll();

        data.put("player", "Guest");
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
}
