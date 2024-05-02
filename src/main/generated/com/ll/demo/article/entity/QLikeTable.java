package com.ll.demo.article.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLikeTable is a Querydsl query type for LikeTable
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLikeTable extends EntityPathBase<LikeTable> {

    private static final long serialVersionUID = -1344313202L;

    public static final QLikeTable likeTable = new QLikeTable("likeTable");

    public final NumberPath<Long> articleId = createNumber("articleId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public QLikeTable(String variable) {
        super(LikeTable.class, forVariable(variable));
    }

    public QLikeTable(Path<? extends LikeTable> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLikeTable(PathMetadata metadata) {
        super(LikeTable.class, metadata);
    }

}

