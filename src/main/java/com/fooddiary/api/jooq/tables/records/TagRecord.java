/*
 * This file is generated by jOOQ.
 */
package com.fooddiary.api.jooq.tables.records;


import com.fooddiary.api.jooq.tables.Tag;

import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TagRecord extends UpdatableRecordImpl<TagRecord> implements Record3<Long, String, LocalDateTime> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>my_food_diarybook.tag.diarybook_id</code>.
     */
    public void setDiarybookId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>my_food_diarybook.tag.diarybook_id</code>.
     */
    public Long getDiarybookId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>my_food_diarybook.tag.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>my_food_diarybook.tag.name</code>.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>my_food_diarybook.tag.create_at</code>.
     */
    public void setCreateAt(LocalDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>my_food_diarybook.tag.create_at</code>.
     */
    public LocalDateTime getCreateAt() {
        return (LocalDateTime) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, String, LocalDateTime> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Long, String, LocalDateTime> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Tag.TAG.DIARYBOOK_ID;
    }

    @Override
    public Field<String> field2() {
        return Tag.TAG.NAME;
    }

    @Override
    public Field<LocalDateTime> field3() {
        return Tag.TAG.CREATE_AT;
    }

    @Override
    public Long component1() {
        return getDiarybookId();
    }

    @Override
    public String component2() {
        return getName();
    }

    @Override
    public LocalDateTime component3() {
        return getCreateAt();
    }

    @Override
    public Long value1() {
        return getDiarybookId();
    }

    @Override
    public String value2() {
        return getName();
    }

    @Override
    public LocalDateTime value3() {
        return getCreateAt();
    }

    @Override
    public TagRecord value1(Long value) {
        setDiarybookId(value);
        return this;
    }

    @Override
    public TagRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public TagRecord value3(LocalDateTime value) {
        setCreateAt(value);
        return this;
    }

    @Override
    public TagRecord values(Long value1, String value2, LocalDateTime value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TagRecord
     */
    public TagRecord() {
        super(Tag.TAG);
    }

    /**
     * Create a detached, initialised TagRecord
     */
    public TagRecord(Long diarybookId, String name, LocalDateTime createAt) {
        super(Tag.TAG);

        setDiarybookId(diarybookId);
        setName(name);
        setCreateAt(createAt);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised TagRecord
     */
    public TagRecord(com.fooddiary.api.jooq.tables.pojos.Tag value) {
        super(Tag.TAG);

        if (value != null) {
            setDiarybookId(value.getDiarybookId());
            setName(value.getName());
            setCreateAt(value.getCreateAt());
            resetChangedOnNotNull();
        }
    }
}
