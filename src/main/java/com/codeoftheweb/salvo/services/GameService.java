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
        String game_self = this.getStateGame(game_player_self, opponent_game_player);
        String game_opponent = this.getStateGame(opponent_game_player, game_player_self);

        if (game_self == "LOST" && game_opponent == "PLAY") {
            state = "LOST";

            // guardar el score
            this.storeScore(game_player_self.getPlayer(), game_player_self.getGame(), 0.0);
        } else if (game_self == "PLAY" && game_opponent == "LOST") {
            state = "WON";

            // guardar el score
            this.storeScore(game_player_self.getPlayer(), game_player_self.getGame(), 1.0);
        } else if (game_self == "LOST" && game_opponent == "LOST") {
            state = "TIE";

            // guardar el score
            this.storeScore(game_player_self.getPlayer(), game_player_self.getGame(), 0.5);
        }

        return state;
    }

    private String getStateGame(GamePlayer self_game_player, GamePlayer opponent_game_player) {
        String state = "PLAY";

        if (opponent_game_player != null && self_game_player != null) {
            int amount_ships = self_game_player.getShips().size();
            int amount_sink_ships = 0;

            Map<String, Object> hits = this.makeHitsDTO(self_game_player, opponent_game_player);
            List hit = (List) hits.getOrDefault("self", null);

            if (hit.size() > 0) {
                Map damages_map = (Map) hit.get(hit.size() - 1);
                Map damages = (Map) damages_map.get("damages");

                for (Ship ship : self_game_player.getShips()) {
                    String key = ship.getType().toLowerCase().replace(" ", "");
                    Long sink = (Long) damages.get(key);

                    if (ship.getLocations().size() == sink) {
                        amount_sink_ships++;
                    }
                }

                if (amount_ships == amount_sink_ships) {
                    state = "LOST";
                }
            }
        }

        return state;
    }

    private void storeScore(Player player, Game game, double score) {
        Date date = new Date();
        Score new_score = new Score(player, game, date, score);

        Score exists_score = scoreRepository.findByPlayerAndGame(player, game);

        if (exists_score == null) {
            scoreRepository.save(new_score);
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
        List<Map<String, Object>> self_list = new ArrayList<Map<String, Object>>();

        if (self == null || opponent == null) {
            return self_list;
        }

        List<Long> self_turn = self.getSalvoes()
                .stream()
                .map(salvo -> salvo.getTurn())
                .collect(Collectors.toList());
        self_turn.sort((o1, o2) -> (int) (o1 - o2));

        // mis ships
        Map<String, Object> self_location_ships = new HashMap<String, Object>();
        self.getShips().forEach(ship -> {
            self_location_ships.put(ship.getType(), ship.getLocations());
        });

        Set<String> name_ships = self_location_ships.keySet();

        //intentos del oponente
        Map<Long, Object> opponent_attemps = new HashMap<Long, Object>();
        opponent.getSalvoes().forEach(salvo -> opponent_attemps.put(salvo.getTurn(), salvo.getLocations()));

        // Mapa de hits totales
        Map<String, Long> total_damages_map = new HashMap<String, Long>();
        total_damages_map.put("carrier", 0L);
        total_damages_map.put("battleship", 0L);
        total_damages_map.put("submarine", 0L);
        total_damages_map.put("destroyer", 0L);
        total_damages_map.put("patrolboat", 0L);

        self_turn.forEach(turn -> {
            // si no existe el turno en el oponente
            long opponent_turn_exists = opponent.getSalvoes()
                    .stream()
                    .filter(salvo -> salvo.getTurn() == turn)
                    .count();

            if (opponent_turn_exists == 1) {
                Map<String, Long> damages_map = new HashMap<String, Long>();
                damages_map.put("carrierHits", 0L);
                damages_map.put("battleshipHits", 0L);
                damages_map.put("submarineHits", 0L);
                damages_map.put("destroyerHits", 0L);
                damages_map.put("patrolboatHits", 0L);

                Map<String, Object> turn_map = new HashMap<String, Object>();
                List<String> hitsLocations = new ArrayList<String>();
                List<String> attemp = (List<String>) opponent_attemps.get(turn);

                if (!attemp.isEmpty()) {
                    attemp.forEach(each_attemp -> {
                        name_ships.forEach(name -> {
                            List<String> each_ship = (List<String>) self_location_ships.get(name);

                            each_ship.forEach(ship_locations -> {
                                // hitLocation
                                if (ship_locations == each_attemp) {
                                    hitsLocations.add(ship_locations);

                                    /** guarda por turno */
                                    String name_key_damage_turn = name.toLowerCase()
                                            .replaceAll(" ", "")
                                            .concat("Hits");

                                    Long hit_value = damages_map.get(name_key_damage_turn);
                                    damages_map.put(name_key_damage_turn, hit_value + 1);

                                    /** guarda el total de hits */
                                    String name_key_damage_total = name.toLowerCase()
                                            .replaceAll(" ", "");

                                    Long total_value = total_damages_map.get(name_key_damage_total);
                                    total_damages_map.put(name_key_damage_total, total_value + 1);
                                }
                            });
                        });
                    });
                }

                turn_map.put("turn", turn);
                turn_map.put("hitLocations", hitsLocations);
                damages_map.putAll(total_damages_map); // combinar mapas de hits
                turn_map.put("damages", damages_map);
                turn_map.put("missed", attemp.size() - hitsLocations.size());

                self_list.add(turn_map);
            }
        });

        return self_list;
    }

    // saber si un usuario está logueado
    public boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    // hacer un ResponseEntity de map
    public ResponseEntity<Map<String, Object>> createEntityResponse(String key, String message, HttpStatus httpStatus) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put(key, message);
        return new ResponseEntity<Map<String, Object>>(map, httpStatus);
    }

    public ResponseEntity<Map<String, Object>> createEntityResponse(Map map, HttpStatus httpStatus) {
        return new ResponseEntity<Map<String, Object>>(map, httpStatus);
    }
}
