package com.bbyiya.dao;

import com.bbyiya.model.TiProducerproducts;

public interface TiProducerproductsMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TiProducerproducts record);

    int insertSelective(TiProducerproducts record);

    TiProducerproducts selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TiProducerproducts record);

    int updateByPrimaryKey(TiProducerproducts record);
}