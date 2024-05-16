package app.dao;



import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DAO<T> {
    protected List<T> db = new ArrayList<>();

    public List<T> getAll() {
        return new ArrayList<>(db);
    }

    public T getById(int id) {
        return db.stream()
                .filter(entity -> getId(entity) == id)
                .findFirst()
                .orElse(null);
    }

    public T create(T entity) {
        db.add(entity);
        return entity;
    }

    public T update(T entity) {
        Optional<T> existing = db.stream()
                .filter(e -> getId(e) == getId(entity))
                .findFirst();
        existing.ifPresent(e -> {
            int index = db.indexOf(e);
            db.set(index, entity);
        });
        return entity;
    }

    public void delete(int id) {
        db.removeIf(entity -> getId(entity) == id);
    }

    protected abstract int getId(T entity);
}
