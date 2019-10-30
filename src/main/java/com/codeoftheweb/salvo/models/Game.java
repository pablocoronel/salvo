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
        self.getShips().forEach(ship -> {
            self_ubicaciones.put(ship.getType(), ship.getLocations());
        });

        Set<String> barcos_nombre = self_ubicaciones.keySet();

        //intentos del oponente
        Map<Long, Object> intentos_opponent = new HashMap<Long, Object>();
        opponent.getSalvoes().forEach(salvo -> intentos_opponent.put(salvo.getTurn(), salvo.getLocations()));

        // hits a mi


/*************************************************/


        turnos_self.forEach(turno -> {
            Map<String, Object> mapa_turno = new HashMap<String, Object>();
            List<String> hitsLocations = new ArrayList<String>();

            Map<String, Object> mapa_damages = new HashMap<String, Object>();


            mapa_turno.put("turn", turno);
            List<String> intento = (List<String>) intentos_opponent.get(turno);

            intento.forEach(un_intento -> {
                barcos_nombre.forEach(b -> {
                    List<String> cada_barco = (List<String>) self_ubicaciones.get(b);

                    String nombre_key_damage_turno = b.toLowerCase().replaceAll(" ", "").concat("Hits");
                    mapa_damages.put(nombre_key_damage_turno, 0);

                    cada_barco.forEach(s -> {
                        // hitLocationes
                        if (s == un_intento) {
                            hitsLocations.add(s);
                        }
                    });

                    // damages
                });

            });

//            Set<String> barcos_nombre_damage = mapa_damages.keySet();

            mapa_turno.put("hitLocations", hitsLocations);
            mapa_turno.put("damages", mapa_damages);
            lista_self.add(mapa_turno);
        });


        System.out.println(lista_self);


        // final
        data.put("self", lista_self);
//        data.put("opponent", lista_opponent);


        return data;
    }


//    public void addScore(Score score){
//        score.setGame(this);
//        this.scores.add(score);
//    }
}
