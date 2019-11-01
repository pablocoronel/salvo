package com.codeoftheweb.salvo.models;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.Player;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date joinDate;

    // relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Ship> ships = new HashSet<Ship>();

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvoes = new HashSet<Salvo>();

    // constructor
    public GamePlayer() {
    }

    public GamePlayer(Game game, Player player, Date fecha) {
        this.game = game;
        this.player = player;
        this.joinDate = fecha;
    }

    // getters y setters
    public long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public Set<Salvo> getSalvoes() {
        return salvoes;
    }

    // metodos particulares
    public Map<String, Object> makeGamePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.getId());
        dto.put("player", this.getPlayer().makePlayerDTO());
        return dto;
    }

    // estado del juego
    public String makeStateDTO() {
        String state = "PLACESHIPS";

        // WAITINGFOROPP
        GamePlayer opponent_game_player = this.getGame().getGamePlayers()
                .stream()
                .filter(gamePlayer -> gamePlayer.getId() != this.getId())
                .findFirst()
                .orElse(null);

        if (this.getShips().isEmpty()) {
            state = "PLACESHIPS";
        } else if (opponent_game_player == null) {
            state = "WAITINGFOROPP";
        } else if (opponent_game_player.getShips().isEmpty()) {
            state = "WAIT";
        }else if(this.getSalvoes().size() > opponent_game_player.getSalvoes().size()){
            state = "WAIT";
        } else {
            state = "PLAY";
        }

        return state;
    }

    // agrega barcos al gamePlayer
    public void addShip(Ship ship) {
        ship.setGamePlayer(this);
        this.ships.add(ship);
    }
}
