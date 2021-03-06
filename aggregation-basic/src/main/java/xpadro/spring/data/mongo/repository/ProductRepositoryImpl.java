package xpadro.spring.data.mongo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import xpadro.spring.data.mongo.domain.Product;
import xpadro.spring.data.mongo.dto.WarehouseSummary;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ProductRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<WarehouseSummary> aggregate(float minPrice, float maxPrice) {
        MatchOperation matchOperation = getMatchOperation(minPrice, maxPrice);
        GroupOperation groupOperation = getGroupOperation();
        ProjectionOperation projectionOperation = getProjectOperation();

        return mongoTemplate.aggregate(Aggregation.newAggregation(
                matchOperation,
                groupOperation,
                projectionOperation
        ), Product.class, WarehouseSummary.class).getMappedResults();
    }

    private MatchOperation getMatchOperation(float minPrice, float maxPrice) {
        Criteria priceCriteria = where("price").gt(minPrice).andOperator(where("price").lt(maxPrice));
        return match(priceCriteria);
    }

    private GroupOperation getGroupOperation() {
        return group("warehouse")
                .last("warehouse").as("warehouse")
                .addToSet("id").as("productIds")
                .avg("price").as("averagePrice")
                .sum("price").as("totalRevenue");
    }

    private ProjectionOperation getProjectOperation() {
        return project("productIds", "averagePrice", "totalRevenue")
                .and("warehouse").previousOperation();
    }
}
