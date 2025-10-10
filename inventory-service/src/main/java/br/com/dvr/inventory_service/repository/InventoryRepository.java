package br.com.dvr.inventory_service.repository;

import br.com.dvr.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository <Inventory, Long> {
    boolean existsBySkuCode(String skuCode);
}
