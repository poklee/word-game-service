package xyz.teamnerds.wordgame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import xyz.teamnerds.wordgame.datastore.WordGameDatastore;

@SpringBootApplication
public class WordGameApplication
{

    @Autowired
    public WordGameDatastore datastore;
    
    
    public static void main(String[] args)
    {
        SpringApplication.run(WordGameApplication.class, args);
    }
    
    

    @Bean
    public WebMvcConfigurer corsConfigurer()
    {
        return new WebMvcConfigurer()
        {
            @Override
            public void addCorsMappings(CorsRegistry registry)
            {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }
}
