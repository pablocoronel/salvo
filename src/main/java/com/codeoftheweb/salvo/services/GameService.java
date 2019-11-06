package com.codeoftheweb.salvo.services;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {
    @Autowired
    private ScoreRepository scoreRepository;

    // estado del juego
    public String makeStateDTO(GamePlayer game_player_self) {
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
        Score nuevo_score = new Score(player, game, date, score);

        Score ya_existe = scoreRepository.findByPlayerAndGame(player, game);

        if (ya_existe == null) {
            scoreRepository.save(nuevo_score);
        }
    }

    // hits
    public Map<String, Object> makeHitsDTO(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("self", this.createHits(self, opponent));
        data.put("opponent", this.createHits(opponent, self));

        return data;
    }

    // logica de hits
    private List<Map<String, Object>> createHits(GamePlayer self, GamePlayer opponent) {
        List<Map<String, Object>> lista_self = new ArrayList<Map<String, Object>>();

        if (self == null || opponent == null) {
            return lista_self;
        }

        List<Long> turnos_self = self.getSalvoes()
                .stream()
                .map(salvo -> salvo.getTurn())
                .collect(Collectors.toList());
        turnos_self.sort((o1, o2) -> (int) (o1 - o2));

        // mis ships
        Map<String, Object> self_ubicaciones = new HashMap<String, Object>();
        self.getShips().forEach(ship -> {
            self_ubicaciones.put(ship.getType(), ship.getLocations());
        });

        Set<String> barcos_nombre = self_ubicaciones.keySet();

        //intentos del oponente
        Map<Long, Object> intentos_opponent = new HashMap<Long, Object>();
        opponent.getSalvoes().forEach(salvo -> intentos_opponent.put(salvo.getTurn(), salvo.getLocations()));

        // Mapa de hits totales
        Map<String, Long> mapa_damages_total = new HashMap<String, Long>();
        mapa_damages_total.put("carrier", 0L);
        mapa_damages_total.put("battleship", 0L);
        mapa_damages_total.put("submarine", 0L);
        mapa_damages_total.put("destroyer", 0L);
        mapa_damages_total.put("patrolboat", 0L);

        turnos_self.forEach(turno -> {
            // si no existe el turno en el oponente
            long existe_turno_oponente = opponent.getSalvoes()
                    .stream()
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
                List<String> intento = (List<String>) intentos_opponent.get(turno);

                if (!intento.isEmpty()) {
                    intento.forEach(un_intento -> {
                        barcos_nombre.forEach(nombre -> {
                            List<String> cada_barco = (List<String>) self_ubicaciones.get(nombre);

                            cada_barco.forEach(ubicacion_barco -> {
                                // hitLocationes
                                if (ubicacion_barco == un_intento) {
                                    hitsLocations.add(ubicacion_barco);

                                    /** guarda por turno */
                                    String nombre_key_damage_turno = nombre.toLowerCase()
                                            .replaceAll(" ", "")
                                            .concat("Hits");

                                    Long valor_hit = mapa_damages.get(nombre_key_damage_turno);
                                    mapa_damages.put(nombre_key_damage_turno, valor_hit + 1);

                                    /** guarda el total de hits */
                                    String nombre_key_damage_total = nombre.toLowerCase()
                                            .replaceAll(" ", "");

                                    Long valor_total = mapa_damages_total.get(nombre_key_damage_total);
                                    mapa_damages_total.put(nombre_key_damage_total, valor_total + 1);
                                }
                            });
                        });
                    });
                }

                mapa_turno.put("turn", turno);
                mapa_turno.put("hitLocations", hitsLocations);
                mapa_damages.putAll(mapa_damages_total); // combinar mapas de hits
                mapa_turno.put("damages", mapa_damages);
                mapa_turno.put("missed", intento.size() - hitsLocations.size());

                lista_self.add(mapa_turno);
            }
        });

        return lista_self;
    }

    // saber si un usuario está logueado
    public boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    // hacer un ResponseEntity de map
    public ResponseEntity<Map<String, Object>> createEntityResponse(String clave, String mensaje, HttpStatus httpStatus) {
        Map<String, Object> mapa = new HashMap<String, Object>();

        mapa.put(clave, mensaje);
        return new ResponseEntity<Map<String, Object>>(mapa, httpStatus);
    }

    public ResponseEntity<Map<String, Object>> createEntityResponse(Map mapa, HttpStatus httpStatus) {
        return new ResponseEntity<Map<String, Object>>(mapa, httpStatus);
    }
}
