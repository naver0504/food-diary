/*
 * This file is generated by jOOQ.
 */
package com.fooddiary.api.jooq.tables;


import com.fooddiary.api.jooq.Keys;
import com.fooddiary.api.jooq.MyFoodDiarybook;
import com.fooddiary.api.jooq.tables.records.MemoRecord;

import java.time.LocalDateTime;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function4;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Memo extends TableImpl<MemoRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>my_food_diarybook.memo</code>
     */
    public static final Memo MEMO = new Memo();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MemoRecord> getRecordType() {
        return MemoRecord.class;
    }

    /**
     * The column <code>my_food_diarybook.memo.id</code>.
     */
    public final TableField<MemoRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>my_food_diarybook.memo.memo</code>.
     */
    public final TableField<MemoRecord, String> MEMO_ = createField(DSL.name("memo"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>my_food_diarybook.memo.create_at</code>.
     */
    public final TableField<MemoRecord, LocalDateTime> CREATE_AT = createField(DSL.name("create_at"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "");

    /**
     * The column <code>my_food_diarybook.memo.diarybook_id</code>.
     */
    public final TableField<MemoRecord, Long> DIARYBOOK_ID = createField(DSL.name("diarybook_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private Memo(Name alias, Table<MemoRecord> aliased) {
        this(alias, aliased, null);
    }

    private Memo(Name alias, Table<MemoRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>my_food_diarybook.memo</code> table reference
     */
    public Memo(String alias) {
        this(DSL.name(alias), MEMO);
    }

    /**
     * Create an aliased <code>my_food_diarybook.memo</code> table reference
     */
    public Memo(Name alias) {
        this(alias, MEMO);
    }

    /**
     * Create a <code>my_food_diarybook.memo</code> table reference
     */
    public Memo() {
        this(DSL.name("memo"), null);
    }

    public <O extends Record> Memo(Table<O> child, ForeignKey<O, MemoRecord> key) {
        super(child, key, MEMO);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : MyFoodDiarybook.MY_FOOD_DIARYBOOK;
    }

    @Override
    public Identity<MemoRecord, Long> getIdentity() {
        return (Identity<MemoRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<MemoRecord> getPrimaryKey() {
        return Keys.KEY_MEMO_PRIMARY;
    }

    @Override
    public Memo as(String alias) {
        return new Memo(DSL.name(alias), this);
    }

    @Override
    public Memo as(Name alias) {
        return new Memo(alias, this);
    }

    @Override
    public Memo as(Table<?> alias) {
        return new Memo(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Memo rename(String name) {
        return new Memo(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Memo rename(Name name) {
        return new Memo(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Memo rename(Table<?> name) {
        return new Memo(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, String, LocalDateTime, Long> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function4<? super Long, ? super String, ? super LocalDateTime, ? super Long, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function4<? super Long, ? super String, ? super LocalDateTime, ? super Long, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
