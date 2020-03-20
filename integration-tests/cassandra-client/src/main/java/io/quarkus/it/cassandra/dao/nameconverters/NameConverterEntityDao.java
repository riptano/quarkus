package io.quarkus.it.cassandra.dao.nameconverters;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;

@Dao
public interface NameConverterEntityDao {
    @Select
    NameConverterEntity findById(int id);

    @Insert
    void save(NameConverterEntity entity);
}