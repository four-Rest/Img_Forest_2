package com.ll.demo.cash.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWithdrawApply is a Querydsl query type for WithdrawApply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWithdrawApply extends EntityPathBase<WithdrawApply> {

    private static final long serialVersionUID = 1467594794L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWithdrawApply withdrawApply = new QWithdrawApply("withdrawApply");

    public final com.ll.demo.global.jpa.QBaseTime _super = new com.ll.demo.global.jpa.QBaseTime(this);

    public final com.ll.demo.member.entity.QMember applicant;

    public final StringPath bankAccountNo = createString("bankAccountNo");

    public final StringPath bankName = createString("bankName");

    public final DateTimePath<java.time.LocalDateTime> cancelDate = createDateTime("cancelDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> cash = createNumber("cash", Long.class);

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

    //inherited
    public final DateTimePath<java.time.LocalDateTime> removedTime = _super.removedTime;

    public final DateTimePath<java.time.LocalDateTime> withdrawDate = createDateTime("withdrawDate", java.time.LocalDateTime.class);

    public QWithdrawApply(String variable) {
        this(WithdrawApply.class, forVariable(variable), INITS);
    }

    public QWithdrawApply(Path<? extends WithdrawApply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWithdrawApply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWithdrawApply(PathMetadata metadata, PathInits inits) {
        this(WithdrawApply.class, metadata, inits);
    }

    public QWithdrawApply(Class<? extends WithdrawApply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.applicant = inits.isInitialized("applicant") ? new com.ll.demo.member.entity.QMember(forProperty("applicant")) : null;
    }

}

