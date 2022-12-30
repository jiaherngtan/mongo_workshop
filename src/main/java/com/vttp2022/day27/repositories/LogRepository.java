package com.vttp2022.day27.repositories;

import java.util.Date;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LogRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void log(String query) {
        Document doc = new Document();
        doc.put("query", query);
        doc.put("date", new Date());
        mongoTemplate.insert(doc, "logs");
    }
}
