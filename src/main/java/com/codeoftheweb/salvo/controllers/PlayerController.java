package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
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
