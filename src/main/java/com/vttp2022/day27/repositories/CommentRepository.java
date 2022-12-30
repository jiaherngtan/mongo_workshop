package com.vttp2022.day27.repositories;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;
import com.vttp2022.day27.models.Comment;

import jakarta.json.JsonArray;

@Repository
public class CommentRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    // db.comments.createIndex({
    // c_text: "text"
    // })

    // db.comments.find(
    // { $text: { $search: "enjoy" }},
    // { commentScore: { $meta: "textScore" }})
    // .sort({ score: 1 })
    // .limit(m)
    // .skip(n)

    public List<Comment> textSearch(
            List<String> matches,
            List<String> notMatch,
            int limit,
            int offset) {

        String[] matchesArr = new String[matches.size()];
        String[] notMatchArr = new String[notMatch.size()];
        for (int i = 0; i < matches.size(); i++)
            matchesArr[i] = matches.get(i);
        for (int i = 0; i < notMatch.size(); i++)
            notMatchArr[i] = notMatch.get(i);

        System.out.println("matches >>> " + Arrays.toString(matchesArr));
        System.out.println("not matches >>> " + Arrays.toString(notMatchArr));

        TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                .matchingAny(matchesArr)
                .notMatchingAny(notMatchArr);

        TextQuery textQuery = (TextQuery) TextQuery.queryText(textCriteria)
                .sortByScore()
                .limit(limit)
                .skip(offset);
        textQuery.setScoreFieldName("score");

        return mongoTemplate.find(textQuery, Document.class, "comments")
                .stream()
                .map(d -> Comment.create(d))
                .toList();
    }

    public Integer addReview(Document doc) {
        Document insert = mongoTemplate.insert(doc, "comments");
        return insert.size();
    }

    public Comment browseCommentById(String id) {

        Criteria c = Criteria.where("_id").is(id);

        Query q = Query.query(c);

        Document d = mongoTemplate.findOne(q, Document.class, "comments");

        return Comment.create(d);
    }

    public Comment updateReview(String id, Comment updatedComment) {

        Criteria c = Criteria.where("_id").is(id);

        Query q = Query.query(c);

        JsonArray edits = updatedComment.toJson(updatedComment.getEdits());

        Update update = new Update();
        update.set("rating", updatedComment.getRating());
        update.set("c_text", updatedComment.getText());
        update.set("edited", edits);
        // Document update = new Document("$set", new Document(id, doc));

        UpdateResult result = mongoTemplate.updateFirst(
                q,
                update,
                Document.class,
                "comments");

        // mongoTemplate.save(d, "comments");

        System.out.println("update result: " + result);
        // return updatedDoc.size();
        return updatedComment;
    }

}
