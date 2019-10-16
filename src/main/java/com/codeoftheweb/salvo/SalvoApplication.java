package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;

@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository) {
        return (args) -> {
            // jugadores
            Player bauer = new Player("j.bauer@ctu.gov");
            Player obrian = new Player("c.obrian@ctu.gov");
            Player almeida = new Player("t.almeida@ctu.gov");
            Player palmer = new Player("d.palmer@whitehouse.gov");

            playerRepository.save(bauer);
            playerRepository.save(obrian);
            playerRepository.save(almeida);
            playerRepository.save(palmer);

            // juegos
            Date date = new Date();
            Date fecha_ahora = new Date();
			Date one_hour_later = Date.from(date.toInstant().plusSeconds(3600));
			Date two_hour_later = Date.from(date.toInstant().plusSeconds(3600 *2));
			Date three_hour_later = Date.from(date.toInstant().plusSeconds(3600 *3));
			Date four_hour_later = Date.from(date.toInstant().plusSeconds(3600 *4));
			Date five_hour_later = Date.from(date.toInstant().plusSeconds(3600 *5));

            Game juego_1 = new Game();
            Game juego_2 = new Game();
            Game juego_3 = new Game();
            Game juego_4 = new Game();
            Game juego_5 = new Game();
            Game juego_6 = new Game();

            gameRepository.save(juego_1);
            gameRepository.save(juego_2);
            gameRepository.save(juego_3);
            gameRepository.save(juego_4);
            gameRepository.save(juego_5);
            gameRepository.save(juego_6);

            // games de players
            GamePlayer gamePlayer_1 = new GamePlayer(juego_1, bauer, fecha_ahora);
            GamePlayer gamePlayer_2 = new GamePlayer(juego_1, obrian, fecha_ahora);
            GamePlayer gamePlayer_3 = new GamePlayer(juego_2, bauer, one_hour_later);
            GamePlayer gamePlayer_4 = new GamePlayer(juego_2, obrian, one_hour_later);
            GamePlayer gamePlayer_5 = new GamePlayer(juego_3, obrian, two_hour_later);
            GamePlayer gamePlayer_6 = new GamePlayer(juego_3, almeida, two_hour_later);
            GamePlayer gamePlayer_7 = new GamePlayer(juego_4, bauer, three_hour_later);
            GamePlayer gamePlayer_8 = new GamePlayer(juego_4, obrian, three_hour_later);
            GamePlayer gamePlayer_9 = new GamePlayer(juego_5, almeida, four_hour_later);
            GamePlayer gamePlayer_10 = new GamePlayer(juego_5, bauer, four_hour_later);
            GamePlayer gamePlayer_11 = new GamePlayer(juego_6, palmer, five_hour_later);

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
        };
    }


}
