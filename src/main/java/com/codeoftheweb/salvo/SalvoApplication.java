package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.models.Ship;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import com.codeoftheweb.salvo.repositories.ShipRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository,
                                      GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository) {
        return (args) -> {
            // jugadores
            Player j_bauer = new Player("j.bauer@ctu.gov");
            Player obrian = new Player("c.obrian@ctu.gov");
            Player almeida = new Player("t.almeida@ctu.gov");
            Player kim_bauer = new Player("kim_bauer@gmail.com");

            playerRepository.save(j_bauer);
            playerRepository.save(obrian);
            playerRepository.save(almeida);
            playerRepository.save(kim_bauer);

            // juegos
            Date date = new Date();
            Date fecha_ahora = new Date();
            Date one_hour_later = Date.from(date.toInstant().plusSeconds(3600));
            Date two_hour_later = Date.from(date.toInstant().plusSeconds(3600 * 2));
            Date three_hour_later = Date.from(date.toInstant().plusSeconds(3600 * 3));
            Date four_hour_later = Date.from(date.toInstant().plusSeconds(3600 * 4));
            Date five_hour_later = Date.from(date.toInstant().plusSeconds(3600 * 5));

            Game juego_1 = new Game();
            Game juego_2 = new Game();
            Game juego_3 = new Game();
            Game juego_4 = new Game();
            Game juego_5 = new Game();
            Game juego_6 = new Game();
            Game juego_7 = new Game();
            Game juego_8 = new Game();

            gameRepository.save(juego_1);
            gameRepository.save(juego_2);
            gameRepository.save(juego_3);
            gameRepository.save(juego_4);
            gameRepository.save(juego_5);
            gameRepository.save(juego_6);
            gameRepository.save(juego_7);
            gameRepository.save(juego_8);

            // games de players
            GamePlayer gamePlayer_1 = new GamePlayer(juego_1, j_bauer, fecha_ahora);
            GamePlayer gamePlayer_2 = new GamePlayer(juego_1, obrian, fecha_ahora);
            GamePlayer gamePlayer_3 = new GamePlayer(juego_2, j_bauer, one_hour_later);
            GamePlayer gamePlayer_4 = new GamePlayer(juego_2, obrian, one_hour_later);
            GamePlayer gamePlayer_5 = new GamePlayer(juego_3, obrian, two_hour_later);
            GamePlayer gamePlayer_6 = new GamePlayer(juego_3, almeida, two_hour_later);
            GamePlayer gamePlayer_7 = new GamePlayer(juego_4, obrian, three_hour_later);
            GamePlayer gamePlayer_8 = new GamePlayer(juego_4, j_bauer, three_hour_later);
            GamePlayer gamePlayer_9 = new GamePlayer(juego_5, almeida, four_hour_later);
            GamePlayer gamePlayer_10 = new GamePlayer(juego_5, j_bauer, four_hour_later);
            GamePlayer gamePlayer_11 = new GamePlayer(juego_6, kim_bauer, five_hour_later);
            GamePlayer gamePlayer_12 = new GamePlayer(juego_8, kim_bauer, five_hour_later);
            GamePlayer gamePlayer_13 = new GamePlayer(juego_8, almeida, five_hour_later);

            gamePlayerRepository.save(gamePlayer_1);
            gamePlayerRepository.save(gamePlayer_2);
            gamePlayerRepository.save(gamePlayer_3);
            gamePlayerRepository.save(gamePlayer_4);
            gamePlayerRepository.save(gamePlayer_5);
            gamePlayerRepository.save(gamePlayer_6);
            gamePlayerRepository.save(gamePlayer_7);
            gamePlayerRepository.save(gamePlayer_8);
            gamePlayerRepository.save(gamePlayer_9);
            gamePlayerRepository.save(gamePlayer_10);
            gamePlayerRepository.save(gamePlayer_11);
            gamePlayerRepository.save(gamePlayer_12);
            gamePlayerRepository.save(gamePlayer_13);

            // ships
            List<String> ubicacion_1 = Arrays.asList("H2", "H3", "H4");
            Ship barco_1 = new Ship("Destroyer", ubicacion_1);
            barco_1.setGamePlayer(gamePlayer_1);
            shipRepository.save(barco_1);

            List<String> ubicacion_2 = Arrays.asList("E1", "F1", "G1");
            Ship barco_2 = new Ship("Submarine", ubicacion_2);
            barco_2.setGamePlayer(gamePlayer_1);
            shipRepository.save(barco_2);

            List<String> ubicacion_3 = Arrays.asList("B4", "B5");
            Ship barco_3 = new Ship("Patrol Boat", ubicacion_3);
            barco_3.setGamePlayer(gamePlayer_1);
            shipRepository.save(barco_3);

            List<String> ubicacion_4 = Arrays.asList("B5", "C5", "D5");
            Ship barco_4 = new Ship("Destroyer", ubicacion_4);
            barco_4.setGamePlayer(gamePlayer_2);
            shipRepository.save(barco_4);

            List<String> ubicacion_5 = Arrays.asList("F1", "F2");
            Ship barco_5 = new Ship("Patrol Boat", ubicacion_5);
            barco_5.setGamePlayer(gamePlayer_2);
            shipRepository.save(barco_5);

            List<String> ubicacion_6 = Arrays.asList("B5", "C5", "D5");
            Ship barco_6 = new Ship("Destroyer", ubicacion_6);
            barco_6.setGamePlayer(gamePlayer_3);
            shipRepository.save(barco_6);

            List<String> ubicacion_7 = Arrays.asList("C6", "C7");
            Ship barco_7 = new Ship("Patrol Boat", ubicacion_7);
            barco_7.setGamePlayer(gamePlayer_3);
            shipRepository.save(barco_7);

            List<String> ubicacion_8 = Arrays.asList("A2", "A3", "A4");
            Ship barco_8 = new Ship("Submarine", ubicacion_8);
            barco_8.setGamePlayer(gamePlayer_4);
            shipRepository.save(barco_8);

            List<String> ubicacion_9 = Arrays.asList("G6", "H6");
            Ship barco_9 = new Ship("Patrol Boat", ubicacion_9);
            barco_9.setGamePlayer(gamePlayer_4);
            shipRepository.save(barco_9);

            List<String> ubicacion_10 = Arrays.asList("B5", "C5", "D5");
            Ship barco_10 = new Ship("Destroyer", ubicacion_10);
            barco_10.setGamePlayer(gamePlayer_5);
            shipRepository.save(barco_10);

            List<String> ubicacion_11 = Arrays.asList("C6", "C7");
            Ship barco_11 = new Ship("Patrol Boat", ubicacion_11);
            barco_11.setGamePlayer(gamePlayer_5);
            shipRepository.save(barco_11);

            List<String> ubicacion_12 = Arrays.asList("A2", "A3", "A4");
            Ship barco_12 = new Ship("Submarine", ubicacion_12);
            barco_12.setGamePlayer(gamePlayer_6);
            shipRepository.save(barco_12);

            List<String> ubicacion_13 = Arrays.asList("G6", "H6");
            Ship barco_13 = new Ship("Patrol Boat", ubicacion_13);
            barco_13.setGamePlayer(gamePlayer_6);
            shipRepository.save(barco_13);

            List<String> ubicacion_14 = Arrays.asList("B5", "C5", "D5");
            Ship barco_14 = new Ship("Destroyer", ubicacion_14);
            barco_14.setGamePlayer(gamePlayer_7);
            shipRepository.save(barco_14);

            List<String> ubicacion_15 = Arrays.asList("C6", "C7");
            Ship barco_15 = new Ship("Patrol Boat", ubicacion_15);
            barco_15.setGamePlayer(gamePlayer_7);
            shipRepository.save(barco_15);

            List<String> ubicacion_16 = Arrays.asList("A2", "A3", "A4");
            Ship barco_16 = new Ship("Submarine", ubicacion_16);
            barco_16.setGamePlayer(gamePlayer_8);
            shipRepository.save(barco_16);

            List<String> ubicacion_17 = Arrays.asList("G6", "H6");
            Ship barco_17 = new Ship("Patrol Boat", ubicacion_17);
            barco_17.setGamePlayer(gamePlayer_8);
            shipRepository.save(barco_17);

            List<String> ubicacion_18 = Arrays.asList("B5", "C5", "D5");
            Ship barco_18 = new Ship("Destroyer", ubicacion_18);
            barco_18.setGamePlayer(gamePlayer_9);
            shipRepository.save(barco_18);

            List<String> ubicacion_19 = Arrays.asList("C6", "C7");
            Ship barco_19 = new Ship("Patrol Boat", ubicacion_19);
            barco_19.setGamePlayer(gamePlayer_9);
            shipRepository.save(barco_19);

            List<String> ubicacion_20 = Arrays.asList("A2", "A3", "A4");
            Ship barco_20 = new Ship("Submarine", ubicacion_20);
            barco_20.setGamePlayer(gamePlayer_10);
            shipRepository.save(barco_20);

            List<String> ubicacion_21 = Arrays.asList("G6", "H6");
            Ship barco_21 = new Ship("Patrol Boat", ubicacion_21);
            barco_21.setGamePlayer(gamePlayer_10);
            shipRepository.save(barco_21);

            List<String> ubicacion_22 = Arrays.asList("B5", "C5", "D5");
            Ship barco_22 = new Ship("Destroyer", ubicacion_22);
            barco_22.setGamePlayer(gamePlayer_11);
            shipRepository.save(barco_22);

            List<String> ubicacion_23 = Arrays.asList("C6", "C7");
            Ship barco_23 = new Ship("Patrol Boat", ubicacion_23);
            barco_23.setGamePlayer(gamePlayer_11);
            shipRepository.save(barco_23);

            List<String> ubicacion_24 = Arrays.asList("B5", "C5", "D5");
            Ship barco_24 = new Ship("Destroyer", ubicacion_24);
            barco_24.setGamePlayer(gamePlayer_12);
            shipRepository.save(barco_24);

            List<String> ubicacion_25 = Arrays.asList("C6", "C7");
            Ship barco_25 = new Ship("Patrol Boat", ubicacion_25);
            barco_25.setGamePlayer(gamePlayer_12);
            shipRepository.save(barco_25);

            List<String> ubicacion_26 = Arrays.asList("A2", "A3", "A4");
            Ship barco_26 = new Ship("Submarine", ubicacion_26);
            barco_26.setGamePlayer(gamePlayer_13);
            shipRepository.save(barco_26);

            List<String> ubicacion_27 = Arrays.asList("G6", "H6");
            Ship barco_27 = new Ship("Patrol Boat", ubicacion_27);
            barco_27.setGamePlayer(gamePlayer_13);
            shipRepository.save(barco_27);

            /*******************/
            // agregar ships a game player
            gamePlayer_1.addShip(barco_1);
            gamePlayer_1.addShip(barco_2);
            gamePlayer_1.addShip(barco_3);
            gamePlayerRepository.save(gamePlayer_1);

            gamePlayer_2.addShip(barco_4);
            gamePlayer_2.addShip(barco_5);
            gamePlayerRepository.save(gamePlayer_2);

            gamePlayer_3.addShip(barco_6);
            gamePlayer_3.addShip(barco_7);
            gamePlayerRepository.save(gamePlayer_3);

            gamePlayer_4.addShip(barco_8);
            gamePlayer_4.addShip(barco_9);
            gamePlayerRepository.save(gamePlayer_4);

            gamePlayer_5.addShip(barco_10);
            gamePlayer_5.addShip(barco_11);
            gamePlayerRepository.save(gamePlayer_5);

            gamePlayer_6.addShip(barco_12);
            gamePlayer_6.addShip(barco_13);
            gamePlayerRepository.save(gamePlayer_6);

            gamePlayer_7.addShip(barco_14);
            gamePlayer_7.addShip(barco_15);
            gamePlayerRepository.save(gamePlayer_7);

            gamePlayer_8.addShip(barco_16);
            gamePlayer_8.addShip(barco_17);
            gamePlayerRepository.save(gamePlayer_8);

            gamePlayer_9.addShip(barco_18);
            gamePlayer_9.addShip(barco_19);
            gamePlayerRepository.save(gamePlayer_9);

            gamePlayer_10.addShip(barco_20);
            gamePlayer_10.addShip(barco_21);
            gamePlayerRepository.save(gamePlayer_10);

            gamePlayer_11.addShip(barco_22);
            gamePlayer_11.addShip(barco_23);
            gamePlayerRepository.save(gamePlayer_11);

            gamePlayer_12.addShip(barco_24);
            gamePlayer_12.addShip(barco_25);
            gamePlayerRepository.save(gamePlayer_12);

            gamePlayer_13.addShip(barco_26);
            gamePlayer_13.addShip(barco_27);
            gamePlayerRepository.save(gamePlayer_13);

        };
    }


}
