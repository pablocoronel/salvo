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
        data.put("hits", this.makeHitsDTO(game_player_self, game_player_opponent));

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

            Map<String, Object> hits = this.makeHitsDTO(self_game_player, opponent_game_player);
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

    // hits
    private Map<String, Object> makeHitsDTO(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("self", this.createHits(self, opponent));
        data.put("opponent", this.createHits(opponent, self));

        return data;
    }

    // logica de hits
    private List<Map<String, Object>> createHits(GamePlayer self, GamePlayer opponent) {
        // json vacio si no hay oponente
        if (self == null || opponent == null) {
            List<Map<String, Object>> lista_self_vacio = new ArrayList<Map<String, Object>>();
            return lista_self_vacio;
        }
        /*****************************************************************************************/
        List<Map<String, Object>> lista_self = new ArrayList<Map<String, Object>>();

        List<Long> turnos_self = self.getSalvoes().stream().map(salvo -> salvo.getTurn()).collect(Collectors.toList());
        turnos_self.sort((o1, o2) -> {
            int res = 0;
            if (o1 < o2) {
                res = -1;
            }
            if (o1 == o2) {
                res = 0;
            }
            if (o1 > o2) {
                res = 1;
            }

            return res;
        });

        // mis ships
        Map<String, Object> self_ubicaciones = new HashMap<String, Object>();
        self.getShips().forEach(ship -> {
            self_ubicaciones.put(ship.getType(), ship.getLocations());
        });

        Set<String> barcos_nombre = self_ubicaciones.keySet();

        //intentos del oponente
        Map<Long, Object> intentos_opponent = new HashMap<Long, Object>();
        opponent.getSalvoes().forEach(salvo -> intentos_opponent.put(salvo.getTurn(), salvo.getLocations()));


        /**
         * Mapa
         */
        Map<String, Long> mapa_damages_total = new HashMap<String, Long>();
        mapa_damages_total.put("carrier", 0L);
        mapa_damages_total.put("battleship", 0L);
        mapa_damages_total.put("submarine", 0L);
        mapa_damages_total.put("destroyer", 0L);
        mapa_damages_total.put("patrolboat", 0L);

        turnos_self.forEach(turno -> {
            // si no existe el turno en el oponente
            long existe_turno_oponente = opponent.getSalvoes().stream()
                    .filter(salvo -> salvo.getTurn() == turno)
                    .count();

            if (existe_turno_oponente == 1) {


                Map<String, Long> mapa_damages = new HashMap<String, Long>();
                mapa_damages.put("carrierHits", 0L);
                mapa_damages.put("battleshipHits", 0L);
                mapa_damages.put("submarineHits", 0L);
                mapa_damages.put("destroyerHits", 0L);
                mapa_damages.put("patrolboatHits", 0L);


                Map<String, Object> mapa_turno = new HashMap<String, Object>();
                List<String> hitsLocations = new ArrayList<String>();


                mapa_turno.put("turn", turno);
                List<String> intento = (List<String>) intentos_opponent.get(turno);

                if (!intento.isEmpty()) {
                    intento.forEach(un_intento -> {
                        barcos_nombre.forEach(b -> {
                            List<String> cada_barco = (List<String>) self_ubicaciones.get(b);


                            cada_barco.forEach(s -> {
                                // hitLocationes
                                if (s == un_intento) {
                                    hitsLocations.add(s);

                                    /**
                                     * guarda por turno
                                     */
                                    String nombre_key_damage_turno = b.toLowerCase().replaceAll(" ", "").concat("Hits");
                                    Long val = mapa_damages.get(nombre_key_damage_turno);

                                    mapa_damages.put(nombre_key_damage_turno, val + 1);

                                    /**
                                     * guarda el total
                                     */
                                    String nombre_key_damage_total = b.toLowerCase().replaceAll(" ", "");
                                    Long val_total = mapa_damages_total.get(nombre_key_damage_total);

                                    mapa_damages_total.put(nombre_key_damage_total, val_total + 1);
                                }
                            });
                        });
                    });
                }


                mapa_turno.put("hitLocations", hitsLocations);


//          combinar mapas de hits
                mapa_damages.putAll(mapa_damages_total);
                mapa_turno.put("damages", mapa_damages);

                mapa_turno.put("missed", 5 - hitsLocations.size());

                lista_self.add(mapa_turno);
            }
        });

        return lista_self;
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
