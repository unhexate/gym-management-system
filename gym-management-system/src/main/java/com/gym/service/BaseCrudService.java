package com.gym.service;

/**
 * Template Method Pattern (Behavioral)
 *
 * Defines the skeleton for CRUD operations (create and delete).
 * Concrete subclasses supply the persistence step; hook methods can be
 * selectively overridden to add validation or pre/post-processing logic.
 *
 * @param <T>  the domain entity type
 * @param <ID> the primary-key type
 */
public abstract class BaseCrudService<T, ID> {

    // -------------------------------------------------------------------------
    // Template method: create
    // -------------------------------------------------------------------------

    /**
     * Create template: validate → beforeSave (hook) → save → afterSave (hook).
     */
    public T create(T entity) {
        validate(entity);
        beforeSave(entity);
        T saved = save(entity);
        afterSave(saved);
        return saved;
    }

    // -------------------------------------------------------------------------
    // Template method: delete
    // -------------------------------------------------------------------------

    /**
     * Delete template: findById → beforeDelete (hook) → performDelete → afterDelete (hook).
     */
    public void delete(ID id) {
        T entity = findById(id);
        beforeDelete(entity);
        performDelete(id);
        afterDelete(entity);
    }

    // -------------------------------------------------------------------------
    // Abstract steps – must be provided by concrete subclasses
    // -------------------------------------------------------------------------

    /** Persist the entity and return the saved copy (with generated id, etc.). */
    protected abstract T save(T entity);

    /** Load an entity by primary key; throw if not found. */
    protected abstract T findById(ID id);

    /** Remove the entity identified by id from the store. */
    protected abstract void performDelete(ID id);

    // -------------------------------------------------------------------------
    // Hook methods – optional overrides for pre/post-processing
    // -------------------------------------------------------------------------

    /** Validate the entity before saving; throw on violation. */
    protected void validate(T entity) { /* no-op by default */ }

    /** Called immediately before persisting the entity. */
    protected void beforeSave(T entity) { /* no-op by default */ }

    /** Called immediately after persisting the entity. */
    protected void afterSave(T saved) { /* no-op by default */ }

    /** Called immediately before deleting the entity. */
    protected void beforeDelete(T entity) { /* no-op by default */ }

    /** Called immediately after deleting the entity. */
    protected void afterDelete(T deleted) { /* no-op by default */ }
}
