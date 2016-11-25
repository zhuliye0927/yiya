package com.bbyiya.dao;

import com.bbyiya.model.UUsers;

public interface UUsersMapper {
	
	/**
	 * 新增用户
	 * @param record
	 * @return
	 */
	int insert(UUsers record);
	/**
	 * 新增用户 返回自增主键userId
	 * @param record
	 * @return
	 */
	int insertReturnKeyId(UUsers record);

	int insertSelective(UUsers record);
	
	UUsers selectByPrimaryKey(Long userid);

	/**
	 * 根据用户userId获取用户
	 * @param userid
	 * @return
	 */
	UUsers getUUsersByUserID(Long userid);

	/**
	 * 根据用户名查询用户
	 * @param username
	 * @return
	 */
	UUsers getUUsersByUserName(String username);

	/**
	 * 对局部赋值的字段进行更新 
	 * @param record
	 * @return
	 */
	int updateByPrimaryKeySelective(UUsers record);
	/**
	 * 全部字段更新
	 * @param record
	 * @return
	 */
	int updateByPrimaryKey(UUsers record);
}