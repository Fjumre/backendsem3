package app.dao;

import java.util.List;

public interface IDAO<T> {

    List<T> getAll();

    T getById(int id);

    T create(T entity);

    T update(T entity);

    void delete(int id);
}
