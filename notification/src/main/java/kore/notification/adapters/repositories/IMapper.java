package kore.notification.adapters.repositories;

public interface IMapper<T, U> {

    T toDomain(U infrastructure);

    U toInfrastructure(T domain);

}
