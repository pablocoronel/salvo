package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

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

    public enum barcos {
        carrier, battleship, Submarine, destroyer, petrolboat
    }

    // hits
    public Map<String, Object> makeHitsDTO(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> data = new HashMap<String, Object>();

        List<Map<String, Object>> lista_self = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> lista_opponent = new ArrayList<Map<String, Object>>();

        List<Long> turnos_self = self.getSalvoes().stream().map(salvo -> salvo.getTurn()).collect(Collectors.toList());

        // mis ships
        Map<String, Object> self_ubicaciones = new HashMap<String, Object>();
        self.getShips().forEach(ship -> self_ubicaciones.put(ship.getType(), ship.getLocations()));

        //intentos del oponente
        Map<Long, Object> intentos_opponent = new HashMap<Long, Object>();
        opponent.getSalvoes().forEach(salvo -> intentos_opponent.put(salvo.getTurn(), salvo.getLocations()));

        // hits a mi
        Map<Long, Object> mapa_hits = new HashMap<Long, Object>();
        List<String> hitsLocations = new ArrayList<String>();
        Iterator<Object> it_ubicaciones = self_ubicaciones.values().iterator();
        List<String> hits_turno = new ArrayList<String>();

        Map<Long, Object> sub_mapa = new HashMap<Long, Object>();




        while (it_ubicaciones.hasNext()) {
            List<String> cada_barco = (List<String>) it_ubicaciones.next();

            turnos_self.forEach(turno -> {

                List<String> intento = (List<String>) intentos_opponent.get(turno);

                intento.forEach(un_intento -> {
                    cada_barco.forEach(s -> {
                        if (s == un_intento) {
//                            hits_turno.add(s);
                            //cambiar la Key del sub_mapa, por el type de ship
                            sub_mapa.put(turno, s);
                        }
                    });
                });

//                mapa_hits.put(turno, hits_turno);
                mapa_hits.put(turno, sub_mapa);
            });
        }


        System.out.println(mapa_hits);
        // final
//        lista_self.addAll();
//        data.put("self", lista_self);
//        data.put("opponent", lista_opponent);


        return data;
    }


//    public void addScore(Score score){
//        score.setGame(this);
//        this.scores.add(score);
//    }
}
