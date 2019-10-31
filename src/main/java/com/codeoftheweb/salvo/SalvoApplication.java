package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository,
                                      GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository,
                                      SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
        return (args) -> {
            // jugadores
            Player j_bauer = new Player("j.bauer@ctu.gov");
            j_bauer.setPassword(passwordEncoder().encode("24"));
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
            gamePlayer_1.addShip(barco_1);
            shipRepository.save(barco_1);

            List<String> ubicacion_2 = Arrays.asList("E1", "F1", "G1");
            Ship barco_2 = new Ship("Submarine", ubicacion_2);
            gamePlayer_1.addShip(barco_2);
            shipRepository.save(barco_2);

            List<String> ubicacion_3 = Arrays.asList("B4", "B5");
            Ship barco_3 = new Ship("Patrol Boat", ubicacion_3);
            gamePlayer_1.addShip(barco_3);
            shipRepository.save(barco_3);

            List<String> ubicacion_4 = Arrays.asList("B5", "C5", "D5");
            Ship barco_4 = new Ship("Destroyer", ubicacion_4);
            gamePlayer_2.addShip(barco_4);
            shipRepository.save(barco_4);

            List<String> ubicacion_5 = Arrays.asList("F1", "F2");
            Ship barco_5 = new Ship("Patrol Boat", ubicacion_5);
            gamePlayer_2.addShip(barco_5);
            shipRepository.save(barco_5);

            List<String> ubicacion_6 = Arrays.asList("B5", "C5", "D5");
            Ship barco_6 = new Ship("Destroyer", ubicacion_6);
            gamePlayer_3.addShip(barco_6);
            shipRepository.save(barco_6);

            List<String> ubicacion_7 = Arrays.asList("C6", "C7");
            Ship barco_7 = new Ship("Patrol Boat", ubicacion_7);
            gamePlayer_3.addShip(barco_7);
            shipRepository.save(barco_7);

            List<String> ubicacion_8 = Arrays.asList("A2", "A3", "A4");
            Ship barco_8 = new Ship("Submarine", ubicacion_8);
            gamePlayer_4.addShip(barco_8);
            shipRepository.save(barco_8);

            List<String> ubicacion_9 = Arrays.asList("G6", "H6");
            Ship barco_9 = new Ship("Patrol Boat", ubicacion_9);
            gamePlayer_4.addShip(barco_9);
            shipRepository.save(barco_9);

            List<String> ubicacion_10 = Arrays.asList("B5", "C5", "D5");
            Ship barco_10 = new Ship("Destroyer", ubicacion_10);
            gamePlayer_5.addShip(barco_10);
            shipRepository.save(barco_10);

            List<String> ubicacion_11 = Arrays.asList("C6", "C7");
            Ship barco_11 = new Ship("Patrol Boat", ubicacion_11);
            gamePlayer_5.addShip(barco_11);
            shipRepository.save(barco_11);

            List<String> ubicacion_12 = Arrays.asList("A2", "A3", "A4");
            Ship barco_12 = new Ship("Submarine", ubicacion_12);
            gamePlayer_6.addShip(barco_12);
            shipRepository.save(barco_12);

            List<String> ubicacion_13 = Arrays.asList("G6", "H6");
            Ship barco_13 = new Ship("Patrol Boat", ubicacion_13);
            gamePlayer_6.addShip(barco_13);
            shipRepository.save(barco_13);

            List<String> ubicacion_14 = Arrays.asList("B5", "C5", "D5");
            Ship barco_14 = new Ship("Destroyer", ubicacion_14);
            gamePlayer_7.addShip(barco_14);
            shipRepository.save(barco_14);

            List<String> ubicacion_15 = Arrays.asList("C6", "C7");
            Ship barco_15 = new Ship("Patrol Boat", ubicacion_15);
            gamePlayer_7.addShip(barco_15);
            shipRepository.save(barco_15);

            List<String> ubicacion_16 = Arrays.asList("A2", "A3", "A4");
            Ship barco_16 = new Ship("Submarine", ubicacion_16);
            gamePlayer_8.addShip(barco_16);
            shipRepository.save(barco_16);

            List<String> ubicacion_17 = Arrays.asList("G6", "H6");
            Ship barco_17 = new Ship("Patrol Boat", ubicacion_17);
            gamePlayer_8.addShip(barco_17);
            shipRepository.save(barco_17);

            List<String> ubicacion_18 = Arrays.asList("B5", "C5", "D5");
            Ship barco_18 = new Ship("Destroyer", ubicacion_18);
            gamePlayer_9.addShip(barco_18);
            shipRepository.save(barco_18);

            List<String> ubicacion_19 = Arrays.asList("C6", "C7");
            Ship barco_19 = new Ship("Patrol Boat", ubicacion_19);
            gamePlayer_9.addShip(barco_19);
            shipRepository.save(barco_19);

            List<String> ubicacion_20 = Arrays.asList("A2", "A3", "A4");
            Ship barco_20 = new Ship("Submarine", ubicacion_20);
            gamePlayer_10.addShip(barco_20);
            shipRepository.save(barco_20);

            List<String> ubicacion_21 = Arrays.asList("G6", "H6");
            Ship barco_21 = new Ship("Patrol Boat", ubicacion_21);
            gamePlayer_10.addShip(barco_21);
            shipRepository.save(barco_21);

            List<String> ubicacion_22 = Arrays.asList("B5", "C5", "D5");
            Ship barco_22 = new Ship("Destroyer", ubicacion_22);
            gamePlayer_11.addShip(barco_22);
            shipRepository.save(barco_22);

            List<String> ubicacion_23 = Arrays.asList("C6", "C7");
            Ship barco_23 = new Ship("Patrol Boat", ubicacion_23);
            gamePlayer_11.addShip(barco_23);
            shipRepository.save(barco_23);

            List<String> ubicacion_24 = Arrays.asList("B5", "C5", "D5");
            Ship barco_24 = new Ship("Destroyer", ubicacion_24);
            gamePlayer_12.addShip(barco_24);
            shipRepository.save(barco_24);

            List<String> ubicacion_25 = Arrays.asList("C6", "C7");
            Ship barco_25 = new Ship("Patrol Boat", ubicacion_25);
            gamePlayer_12.addShip(barco_25);
            shipRepository.save(barco_25);

            List<String> ubicacion_26 = Arrays.asList("A2", "A3", "A4");
            Ship barco_26 = new Ship("Submarine", ubicacion_26);
            gamePlayer_13.addShip(barco_26);
            shipRepository.save(barco_26);

            List<String> ubicacion_27 = Arrays.asList("G6", "H6");
            Ship barco_27 = new Ship("Patrol Boat", ubicacion_27);
            gamePlayer_13.addShip(barco_27);
            shipRepository.save(barco_27);

            // salvos
            List<String> salvo_ubicacion_1 = Arrays.asList("B5", "C5", "F1");
            Salvo salvo_1 = new Salvo(1, gamePlayer_1, salvo_ubicacion_1);
            salvoRepository.save(salvo_1);

            List<String> salvo_ubicacion_2 = Arrays.asList("B4", "B5", "B6");
            Salvo salvo_2 = new Salvo(1, gamePlayer_2, salvo_ubicacion_2);
            salvoRepository.save(salvo_2);

            List<String> salvo_ubicacion_3 = Arrays.asList("F2", "D5");
            Salvo salvo_3 = new Salvo(2, gamePlayer_1, salvo_ubicacion_3);
            salvoRepository.save(salvo_3);

            List<String> salvo_ubicacion_4 = Arrays.asList("E1", "H3", "A2");
            Salvo salvo_4 = new Salvo(2, gamePlayer_2, salvo_ubicacion_4);
            salvoRepository.save(salvo_4);

            List<String> salvo_ubicacion_5 = Arrays.asList("A2", "A4", "G6");
            Salvo salvo_5 = new Salvo(1, gamePlayer_3, salvo_ubicacion_5);
            salvoRepository.save(salvo_5);

            List<String> salvo_ubicacion_6 = Arrays.asList("B5", "D5", "C7");
            Salvo salvo_6 = new Salvo(1, gamePlayer_4, salvo_ubicacion_6);
            salvoRepository.save(salvo_6);

            List<String> salvo_ubicacion_7 = Arrays.asList("A3", "H6");
            Salvo salvo_7 = new Salvo(2, gamePlayer_3, salvo_ubicacion_7);
            salvoRepository.save(salvo_7);

            List<String> salvo_ubicacion_8 = Arrays.asList("C5", "C6");
            Salvo salvo_8 = new Salvo(2, gamePlayer_4, salvo_ubicacion_8);
            salvoRepository.save(salvo_8);
            // row #

            List<String> salvo_ubicacion_9 = Arrays.asList("G6", "H6", "A4");
            Salvo salvo_9 = new Salvo(1, gamePlayer_5, salvo_ubicacion_9);
            salvoRepository.save(salvo_9);

            List<String> salvo_ubicacion_10 = Arrays.asList("H1", "H2", "H3");
            Salvo salvo_10 = new Salvo(1, gamePlayer_6, salvo_ubicacion_10);
            salvoRepository.save(salvo_10);

            List<String> salvo_ubicacion_11 = Arrays.asList("A2", "A3", "D8");
            Salvo salvo_11 = new Salvo(2, gamePlayer_5, salvo_ubicacion_11);
            salvoRepository.save(salvo_11);

            List<String> salvo_ubicacion_12 = Arrays.asList("E1", "F2", "G3");
            Salvo salvo_12 = new Salvo(2, gamePlayer_6, salvo_ubicacion_12);
            salvoRepository.save(salvo_12);

            List<String> salvo_ubicacion_13 = Arrays.asList("A3", "A4", "F7");
            Salvo salvo_13 = new Salvo(1, gamePlayer_7, salvo_ubicacion_13);
            salvoRepository.save(salvo_13);

            List<String> salvo_ubicacion_14 = Arrays.asList("B5", "C6", "H1");
            Salvo salvo_14 = new Salvo(1, gamePlayer_8, salvo_ubicacion_14);
            salvoRepository.save(salvo_14);

            List<String> salvo_ubicacion_15 = Arrays.asList("A2", "G6", "H6");
            Salvo salvo_15 = new Salvo(2, gamePlayer_7, salvo_ubicacion_15);
            salvoRepository.save(salvo_15);

            List<String> salvo_ubicacion_16 = Arrays.asList("C5", "C7", "D5");
            Salvo salvo_16 = new Salvo(2, gamePlayer_8, salvo_ubicacion_16);
            salvoRepository.save(salvo_16);

            List<String> salvo_ubicacion_17 = Arrays.asList("A1", "A2", "A3");
            Salvo salvo_17 = new Salvo(1, gamePlayer_9, salvo_ubicacion_17);
            salvoRepository.save(salvo_17);

            List<String> salvo_ubicacion_18 = Arrays.asList("B5", "B6", "C7");
            Salvo salvo_18 = new Salvo(1, gamePlayer_10, salvo_ubicacion_18);
            salvoRepository.save(salvo_18);

            List<String> salvo_ubicacion_19 = Arrays.asList("G6", "G7", "G8");
            Salvo salvo_19 = new Salvo(2, gamePlayer_9, salvo_ubicacion_19);
            salvoRepository.save(salvo_19);

            List<String> salvo_ubicacion_20 = Arrays.asList("C6", "D6", "E6");
            Salvo salvo_20 = new Salvo(2, gamePlayer_10, salvo_ubicacion_20);
            salvoRepository.save(salvo_20);

            List<String> salvo_ubicacion_21 = Arrays.asList("H1", "H8");
            Salvo salvo_21 = new Salvo(3, gamePlayer_10, salvo_ubicacion_21);
            salvoRepository.save(salvo_21);

            // scores
            Score score_1 = new Score(j_bauer, juego_1, one_hour_later, 1);
            scoreRepository.save(score_1);

            Score score_2 = new Score(obrian, juego_1, one_hour_later, 0);
            scoreRepository.save(score_2);

            Score score_3 = new Score(j_bauer, juego_2, one_hour_later, 0.5);
            scoreRepository.save(score_3);

            Score score_4 = new Score(obrian, juego_2, one_hour_later, 0.5);
            scoreRepository.save(score_4);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {
    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName -> {
            Player player = playerRepository.findByUserName(inputName);
            if (player != null) {
                return new User(player.getUserName(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                throw new UsernameNotFoundException("Unknown user: " + inputName);
            }
        });
    }
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //rutas con permisos
        http.authorizeRequests()
                .antMatchers("/web/**").permitAll()
                .antMatchers("/api/game_view/*").hasAuthority("USER")
//                .antMatchers("/rest/**").hasAuthority("ADMIN")
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/games").permitAll()
                .antMatchers("/api/players").permitAll();

        // atributos del objeto recibido desde el form
        http.formLogin()
                .usernameParameter("name")
                .passwordParameter("pwd")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");


        // turn off checking for CSRF tokens
        http.csrf().disable();
        http.headers().frameOptions().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }

    }

}