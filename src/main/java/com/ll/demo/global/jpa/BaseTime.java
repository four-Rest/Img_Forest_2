package com.ll.demo.global.jpa;

import com.ll.demo.global.entity.BaseEntity;
import com.ll.demo.global.util.Ut;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // @CreatedDate, @LastModifiedDate를 사용하기 위해 필요
@Getter
@ToString(callSuper = true)
public abstract class BaseTime extends BaseEntity {
    @CreatedDate
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime modifyDate;

    public String getModelName() {
        return Ut.str.lcfirst(this.getClass().getSimpleName());
    }
}