package com.jettech.domain;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jettech.entity.BaseEntity;

public abstract class BaseModel {

	private static final String ENTITY_PACKAGE = "com.jettech.db.entity";

	private Integer id;
	private String createUser;
	private String editUser;
	private Date createTime;
	private Date editTime;

	protected BaseModel() {

	}

	protected BaseModel(BaseEntity entity) {
		this();
		parseEntity(entity);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getEditUser() {
		return editUser;
	}

	public void setEditUser(String editUser) {
		this.editUser = editUser;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	// @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	// @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getEditTime() {
		return editTime;
	}

	public void setEditTime(Date editTime) {
		this.editTime = editTime;
	}

	protected void name() {

	}

	/**
	 * 将实体对象的属性值赋值给模型对象(需要保证属性的名称一致),此方法存在不Work的情况！！！
	 * 
	 * @param entity
	 * @throws Throwable
	 */

	protected void parse(BaseEntity entity) throws Exception {
		if (entity == null) {
			throw new Exception("entity is null.");
		} else {
			copyObjectFieldValue(entity, this);
		}
	}

	/**
	 * 复制BaseEntity的基础属性到BaseModel,(基础的Model实现其它属性的复制)
	 * 
	 * @param entity
	 */
	protected void parseEntity(BaseEntity entity) {
		this.setCreateTime(entity.getCreateTime());
		this.setCreateUser(entity.getCreateUser());
		this.setEditTime(entity.getEditTime());
		this.setEditUser(entity.getEditUser());
		this.setId(entity.getId());
	}

	private void copyObjectFieldValue(Object source, Object target)
	        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		// Class<? extends BaseEntity> clzEntity = entity.getClass();
		// System.out.println("entity:" + clzEntity.getName());
		//
		// Class<? extends BaseModel> clzModel = baseModel.getClass();
		// System.out.println("model:" + clzModel.getName());

		Class<?> clzSource = source.getClass();
		Class<?> clzTarget = target.getClass();

		Field[] modelFields = clzTarget.getDeclaredFields();
		Map<String, Field> mfMap = new HashMap<>();
		for (Field mf : modelFields) {
			mfMap.put(mf.getName(), mf);
		}

		// 获取实体类的所有属性，返回Field数组
		Field[] fields = clzSource.getDeclaredFields();
		for (Field field : fields) {
			String name = field.getName();
			String typeName = field.getType().getName();
			if (typeName.equals("java.util.List") || typeName.endsWith("java.util.Set"))
				continue;
			if (field.getType().isArray())
				continue;
			if (name.equals("serialVersionUID") || name.equals("handler") || name.equals("_methods_")
			        || name.equals("_filter_signature"))
				continue;
			if (field.getType().getGenericSuperclass() != null
			        && field.getType().getGenericSuperclass().equals(BaseEntity.class)) {
				// System.out.println("field:" + name + " is Entity");
				continue;
			}
			// if(field.getClass().isAnnotationPresent(BaseEntity))
			// System.out.println("field " + name + ".type:" +
			// field.getGenericType());// 打印该类的所有属性类型
			if (!mfMap.containsKey(name)) {
				// 没有找到对应的属性
				// throw new Throwable(clzTarget.getName() + " not found field:"
				// + name);
				System.out.println(clzTarget.getName() + " not found field:" + name);
			} else {
				Field mf = mfMap.get(name);
				// 首字符大写
				name = name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
				String type = mf.getGenericType().toString(); // 获取属性的类型
				Method m1 = clzSource.getMethod("get" + name);
				Object value = m1.invoke(source);
				// System.out.println("field " + name + ":" + value);
				if (value != null) {

					if (type.equals("class java.lang.String")) {
						Method m2 = clzTarget.getMethod("set" + name, String.class);
						m2.invoke(target, value);
					} else if (type.equals("class java.lang.Integer")) {
						Method m2 = clzTarget.getMethod("set" + name, Integer.class);
						m2.invoke(target, value);
					} else if (type.equals("class java.lang.Boolean")) {
						Method m2 = clzTarget.getMethod("set" + name, Boolean.class);
						m2.invoke(target, value);
					} else if (type.equals("class java.util.Date")) {
						Method m2 = clzTarget.getMethod("set" + name, Date.class);
						m2.invoke(target, value);
					} else if (type.equals("class java.lang.Double")) {
						Method m2 = clzTarget.getMethod("set" + name, Double.class);
						m2.invoke(target, value);
					} else if (type.equals("class java.lang.Enum")) {
						Method m2 = clzTarget.getMethod("set" + name, Enum.class);
						m2.invoke(target, value);
					} else if (type.equals("class java.lang.Float")) {
						Method m2 = clzTarget.getMethod("set" + name, Float.class);
						m2.invoke(target, value);
					} else if (type.equals("class java.lang.Short")) {
						Method m2 = clzTarget.getMethod("set" + name, Short.class);
						m2.invoke(target, value);
					} else if (type.equals("class java.lang.Number")) {
						Method m2 = clzTarget.getMethod("set" + name, Number.class);
						m2.invoke(target, value);
					} else if (type.equals("boolean")) {
						Method m2 = (Method) clzTarget.getMethod(field.getName());
						Boolean val = (Boolean) m2.invoke(target);
						if (val != null) {
							System.out.println("boolean type:" + val);
						}
					} else if (mf.getType().isEnum()) {
						// 枚举类型,使用原对象的属性的相同枚举类型
						Method m2 = clzTarget.getMethod("set" + name, field.getType());
						m2.invoke(target, value);
					} else {
						System.out.println("not match field " + name + ":" + value);
					}
				}
			}
		}
	}

	protected BaseEntity toEntity() {
		BaseEntity entity = null;
		String modelName = this.getClass().getName();
		// System.out.println("model:" + modelName);
		String entityName = modelName.substring(modelName.lastIndexOf(".") + 1);
		entityName = entityName.substring(0, entityName.toLowerCase().lastIndexOf("model"));
		entityName = ENTITY_PACKAGE + "." + entityName;
		// System.out.println("entity:" + entityName);
		try {
			Class<?> clzEntity = this.getClass().getClassLoader().loadClass(entityName);
			entity = (BaseEntity) clzEntity.newInstance();
			copyObjectFieldValue(this, entity);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return entity;
	}
}
