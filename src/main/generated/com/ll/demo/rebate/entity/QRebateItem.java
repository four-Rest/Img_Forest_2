package com.ll.demo.rebate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRebateItem is a Querydsl query type for RebateItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRebateItem extends EntityPathBase<RebateItem> {

    private static final long serialVersionUID = -877358240L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRebateItem rebateItem = new QRebateItem("rebateItem");

    public final com.ll.demo.global.jpa.QBaseTime _super = new com.ll.demo.global.jpa.QBaseTime(this);

    public final com.ll.demo.article.entity.QArticle article;

    public final com.ll.demo.member.entity.QMember buyer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    public final DateTimePath<java.time.LocalDateTime> eventDate = createDateTime("eventDate", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath modelName = _super.modelName;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedTime = _super.modifiedTime;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final com.ll.demo.order.entity.QOrderItem orderItem;

    public final DateTimePath<java.time.LocalDateTime> payDate = createDateTime("payDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> payPrice = createNumber("payPrice", Long.class);

    public final DateTimePath<java.time.LocalDateTime> rebateDate = createDateTime("rebateDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> rebatePrice = createNumber("rebatePrice", Long.class);

    public final NumberPath<Double> rebateRate = createNumber("rebateRate", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> removedTime = _super.removedTime;

    public final com.ll.demo.member.entity.QMember seller;

    public QRebateItem(String variable) {
        this(RebateItem.class, forVariable(variable), INITS);
    }

    public QRebateItem(Path<? extends RebateItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRebateItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRebateItem(PathMetadata metadata, PathInits inits) {
        this(RebateItem.class, metadata, inits);
    }

    public QRebateItem(Class<? extends RebateItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new com.ll.demo.article.entity.QArticle(forProperty("article"), inits.get("article")) : null;
        this.buyer = inits.isInitialized("buyer") ? new com.ll.demo.member.entity.QMember(forProperty("buyer")) : null;
        this.orderItem = inits.isInitialized("orderItem") ? new com.ll.demo.order.entity.QOrderItem(forProperty("orderItem"), inits.get("orderItem")) : null;
        this.seller = inits.isInitialized("seller") ? new com.ll.demo.member.entity.QMember(forProperty("seller")) : null;
    }

}

