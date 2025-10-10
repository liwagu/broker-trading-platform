package io.github.liwagu.trading.repository;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for managing buying power.
 * The entity is a simple BigDecimal.
 * The key is a String, associated to a unique portfolio ID.
 *
 */
public interface BuyingPowerRepository extends CrudRepository<BuyingPowerEntity, String> {

}

