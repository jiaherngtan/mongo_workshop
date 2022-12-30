package com.vttp2022.day27.services;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vttp2022.day27.models.Game;
import com.vttp2022.day27.repositories.GameRepository;
import com.vttp2022.day27.repositories.LogRepository;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private LogRepository logRepository;

    public List<Game> gameSearch(String query, Integer limit, Integer offset) {

        List<String> include = new LinkedList<>();
        List<String> exclude = new LinkedList<>();

        for (String w : query.split(" "))
            if (w.startsWith("-"))
                exclude.add(w);
            else
                include.add(w);

        try {
            return gameRepository.gameSearch(include, exclude, limit, offset)
                    .stream()
                    .toList();
        } finally {
            logRepository.log(query);
        }
    }

    public List<Game> browseGamesByRank(Integer limit, Integer offset) {
        return gameRepository.browseGamesByRank(limit, offset);
    }

    public Game browseGameById(String id) {
        return gameRepository.browseGameById(id);
    }

    public Game browseGameByIdWithComments(String id) {
        return gameRepository.browseGameByIdWithComments(id);
    }

    public Game browseGameByGid(String id) {
        return gameRepository.browseGameByGid(id);
    }

    public List<Game> browseGamesByRating(String query) {
        return gameRepository.browseGamesByRating(query);
    }
}
