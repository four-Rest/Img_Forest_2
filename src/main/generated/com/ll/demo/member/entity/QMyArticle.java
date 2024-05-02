package com.ll.demo.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMyArticle is a Querydsl query type for MyArticle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMyArticle extends EntityPathBase<MyArticle> {

    private static final long serialVersionUID = 1040527433L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMyArticle myArticle = new QMyArticle("myArticle");

    public final com.ll.demo.global.entity.QBaseEntity _super = new com.ll.demo.global.entity.QBaseEntity(this);

    public final com.ll.demo.article.entity.QArticle article;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedTime = _super.modifiedTime;

    public final QMember owner;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> removedTime = _super.removedTime;

    public QMyArticle(String variable) {
        this(MyArticle.class, forVariable(variable), INITS);
    }

    public QMyArticle(Path<? extends MyArticle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMyArticle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMyArticle(PathMetadata metadata, PathInits inits) {
        this(MyArticle.class, metadata, inits);
    }

    public QMyArticle(Class<? extends MyArticle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new com.ll.demo.article.entity.QArticle(forProperty("article"), inits.get("article")) : null;
        this.owner = inits.isInitialized("owner") ? new QMember(forProperty("owner")) : null;
    }

}

