package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date creationDate;

    // constructor
    public Game() {
        this.creationDate = new Date();
    }

    // constructor con seteo de fecha
    public Game(Date date) {
        this.creationDate = date;
    }

    // getters y setters
    public long getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    // relaciones
    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    Set<Score> scores;

    // metodos particulares
    public Map<String, Object> makeGameDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("created", this.getCreationDate());
        dto.put("gamePlayers", this.getGamePlayers().stream().map(gamePlayer -> gamePlayer.makeGamePlayerDTO()).collect(Collectors.toList()));

        dto.put("scores", this.getScores().stream().map(score -> score.makeScoreDTO()));
        return dto;
    }

    // hits
    public Map<String, Object> makeHitsDTO(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("self", this.createHits(self, opponent));
        data.put("opponent", this.createHits(opponent, self));

        return data;
    }

    // logica de hits
    public List<Map<String, Object>> createHits(GamePlayer self, GamePlayer opponent) {
        // json vacio si no hay oponente
        if (self == null || opponent == null) {
            Map<String, Object> mapa_vacio = new HashMap<String, Object>();

            mapa_vacio.put("turn", "");
            List<String> hitsLocations_vacio = new ArrayList<String>();
            mapa_vacio.put("hitLocations", hitsLocations_vacio);

            // combinar mapas de hits
            Map<String, Long> mapa_vacio_damages = new HashMap<String, Long>();
            mapa_vacio_damages.put("carrier", 0L);
            mapa_vacio_damages.put("battleship", 0L);
            mapa_vacio_damages.put("submarine", 0L);
            mapa_vacio_damages.put("destroyer", 0L);
            mapa_vacio_damages.put("patrolboat", 0L);
            mapa_vacio_damages.put("carrierHits", 0L);
            mapa_vacio_damages.put("battleshipHits", 0L);
            mapa_vacio_damages.put("submarineHits", 0L);
            mapa_vacio_damages.put("destroyerHits", 0L);
            mapa_vacio_damages.put("patrolboatHits", 0L);

            mapa_vacio.put("damages", mapa_vacio_damages);

            mapa_vacio.put("missed", 0);

            List<Map<String, Object>> lista_self_vacio = new ArrayList<Map<String, Object>>();
            lista_self_vacio.add(mapa_vacio);

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


//    public void addScore(Score score){
//        score.setGame(this);
//        this.scores.add(score);
//    }
}
