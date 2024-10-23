package ca.jrvs.apps.jdbc.STOCKQUOTE;

import java.util.Optional;

public interface CrudDao<T, ID> {

    T save(T entity) throws IllegalArgumentException;

    Optional<T> findById(ID id) throws IllegalArgumentException;

    Iterable<T> findAll();

    void deleteById(ID id) throws IllegalArgumentException;

    void deleteAll();
}
