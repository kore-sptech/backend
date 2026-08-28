package kore.notification.application.repositories;

public interface ISaveEntityRepository<T> {

    T save(T entity);

}
