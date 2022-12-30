package com.vttp2022.day27.repositories;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;

import com.vttp2022.day27.models.Game;

@Repository
public class GameRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Game> gameSearch(List<String> include, List<String> exclude) {
        return gameSearch(include, exclude, 25, 0);
    }

    public List<Game> gameSearch(List<String> include, List<String> exclude, int limit) {
        return gameSearch(include, exclude, limit, 0);
    }

    public List<Game> gameSearch(List<String> include, List<String> exclude, int limit, int offset) {

        TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                .matchingAny(include.toArray(new String[include.size()]))
                .notMatchingAny(exclude.toArray(new String[exclude.size()]));

        TextQuery textQuery = (TextQuery) TextQuery.queryText(textCriteria)
                .includeScore("search_score")
                .sortByScore()
                .limit(limit)
                .skip(offset);

        return mongoTemplate.find(textQuery, Document.class, "games")
                .stream()
                .map(d -> Game.create(d))
                .toList();
    }

    public List<Game> browseGamesByRank(Integer limit, Integer offset) {

        SortOperation sortByRanking = Aggregation.sort(Sort.by(Direction.ASC, "ranking"));
        Aggregation pipeline = Aggregation.newAggregation(sortByRanking);
        AggregationResults<Document> results = mongoTemplate.aggregate(pipeline, "games", Document.class);

        List<Game> boardgames = new LinkedList<>();
        for (Document d : results)
            boardgames.add(Game.create(d));

        return boardgames;
    }

    public Game browseGameById(String id) {

        Document d = mongoTemplate.findById(id, Document.class, "games");

        return Game.create(d);
    }

    public Game browseGameByIdWithComments(String id) {

        Game game = browseGameById(id);

        // match the name
        MatchOperation findName = Aggregation.match(
                Criteria.where("name").is(game.getName()));

        // lookup comments
        LookupOperation findComments = Aggregation.lookup(
                "comments",
                "gid",
                "gid",
                "reviews");

        // create pipeline
        Aggregation pipeline = Aggregation.newAggregation(findName, findComments);

        // query
        AggregationResults<Document> result = mongoTemplate.aggregate(pipeline, "games", Document.class);

        Game g = new Game();
        for (Document d : result)
            g = Game.create(d);

        return g;
    }

    public Game browseGameByGid(String id) {

        Criteria c = Criteria.where("gid").is(Integer.parseInt(id));

        Query q = Query.query(c);

        Document d = mongoTemplate.findOne(q, Document.class, "games");

        return Game.create(d);
    }

    public List<Game> browseGamesByRating(String query) {

        // sort comments
        SortOperation sortComments = null;
        if (query.equals("highest"))
            sortComments = Aggregation.sort(Sort.by(Direction.DESC, "rating"));
        else if (query.equals("lowest"))
            sortComments = Aggregation.sort(Sort.by(Direction.ASC, "rating"));

        LookupOperation findComments = Aggregation.lookup(
                "comments",
                "gid",
                "gid",
                "reviews");

        // create the pipeline
        Aggregation pipeline = Aggregation.newAggregation(findComments, sortComments);
        Aggregation.limit(1);

        // query the collection
        AggregationResults<Document> results = mongoTemplate.aggregate(pipeline, "games", Document.class);

        List<Game> games = new LinkedList<>();
        for (Document d : results)
            games.add(Game.create(d));

        return games;
    }

}
