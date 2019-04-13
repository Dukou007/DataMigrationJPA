package com.jettech.repostory;

import com.jettech.entity.QualityCodeMapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QualityCodeMapItemRepository extends JpaRepository<QualityCodeMapItem, Integer>{
	@Query(value = "select * from code_map_item m where m.code_map_id=?1 ",nativeQuery = true)
	public List<QualityCodeMapItem> findByCodeMapId(Integer codeMapId);
}
