package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import com.codeoftheweb.salvo.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/api")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    GameService gameService;

    // crear un player
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String email, @RequestParam String password) {
        ResponseEntity<Map<String, Object>> response = null;

        // validar data ingresada
        if (email.isEmpty() || password.isEmpty()) {
            response = gameService.createEntityResponse("error", "Empty email or password", HttpStatus.FORBIDDEN);
        }

        // consultar si ya existe un usuario igual
        Player player = playerRepository.findByUserName(email);
        if (player != null) {
            response = gameService.createEntityResponse("error", "The user already exists", HttpStatus.CONFLICT);
        } else {
            Player new_player = new Player();
            new_player.setUserName(email);
            new_player.setPassword(passwordEncoder.encode(password));

            playerRepository.save(new_player);

            response = gameService.createEntityResponse("success", "User created", HttpStatus.CREATED);
        }

        return response;
    }
}
