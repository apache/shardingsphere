package org.apache.shardingsphere.example.common.jpa.repository;

import org.apache.shardingsphere.example.common.entity.Country;
import org.apache.shardingsphere.example.common.repository.CountryRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Transactional
public class CountryRepositoryImpl implements CountryRepository {

    private final static AtomicLong ID_INCREASE = new AtomicLong();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void createTableIfNotExists() {
        throw new UnsupportedOperationException("createTableIfNotExists for JPA");
    }

    @Override
    public void dropTable() {
        throw new UnsupportedOperationException("truncateTable for JPA");
    }

    @Override
    public void truncateTable() {
        throw new UnsupportedOperationException("dropTable for JPA");
    }

    @Override
    public Long insert(Country country) {
        long id = ID_INCREASE.incrementAndGet();
        country.setId(id);
        entityManager.persist(country);
        return id;
    }

    @Override
    public void delete(Long id) {
        Query query = entityManager.createQuery("DELETE FROM CountryEntity o WHERE o.id = ?1");
        query.setParameter(1, id);
        query.executeUpdate();
    }

    @Override
    public List<Country> selectAll() {
        return (List<Country>) entityManager.createQuery("SELECT o FROM CountryEntity o").getResultList();
    }
}
