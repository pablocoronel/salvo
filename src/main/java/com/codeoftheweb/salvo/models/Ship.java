package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String type;

    // constructor
    public Ship() {
    }

    public Ship(String type, List<String> shipLocations) {
        this.type = type;
        this.shipLocations = shipLocations;
    }

    // relaciones
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name = "ship_locations")
    private List<String> shipLocations;

    // getters y setters
    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getShipLocations() {
        return shipLocations;
    }

    public void setShipLocations(List<String> shipLocations) {
        this.shipLocations = shipLocations;
    }

    // metodos particulares
    public Map<String, Object> makeShipDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();

        dto.put("type", this.getType());
        dto.put("locations", this.getShipLocations());

        return dto;
    }
}
