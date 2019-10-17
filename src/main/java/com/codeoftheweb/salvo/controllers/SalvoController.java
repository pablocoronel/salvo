package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @RequestMapping("/games")
    public List<Map<String, Object>> getGames() {
        // listado de ID's
        List<Game> lista_juegos = gameRepository.findAll();
        List<Long> lista_id = lista_juegos.stream()
                .map(game -> game.getId())
                .collect(Collectors.toList());

        // listado de maps (id's)
        return gameRepository.findAll().stream().map(game -> game.makeGameDTO(game)).collect(Collectors.toList());
    }
}
