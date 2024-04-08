package com.ll.demo.article.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPurchasedArticle is a Querydsl query type for PurchasedArticle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurchasedArticle extends EntityPathBase<PurchasedArticle> {

    private static final long serialVersionUID = 1099956732L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPurchasedArticle purchasedArticle = new QPurchasedArticle("purchasedArticle");

    public final com.ll.demo.global.jpa.QBaseTime _super = new com.ll.demo.global.jpa.QBaseTime(this);

    public final QArticle article;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath modelName = _super.modelName;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedTime = _super.modifiedTime;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final com.ll.demo.member.entity.QMember owner;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> removedTime = _super.removedTime;

    public QPurchasedArticle(String variable) {
        this(PurchasedArticle.class, forVariable(variable), INITS);
    }

    public QPurchasedArticle(Path<? extends PurchasedArticle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPurchasedArticle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPurchasedArticle(PathMetadata metadata, PathInits inits) {
        this(PurchasedArticle.class, metadata, inits);
    }

    public QPurchasedArticle(Class<? extends PurchasedArticle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new QArticle(forProperty("article"), inits.get("article")) : null;
        this.owner = inits.isInitialized("owner") ? new com.ll.demo.member.entity.QMember(forProperty("owner")) : null;
    }

}

