package com.auction.domain.model;

/**
 * Lớp cơ sở của mọi đối tượng có định danh. Hai entity bằng nhau khi cùng lớp và cùng id.
 */
public abstract class Entity {

    private final String id;

    protected Entity(String id) {
        this.id = Guard.requireNotBlank(id, "id");
    }

    public String getId() {
        return id;
    }

    /** Mô tả ngắn gọn đối tượng, mỗi lớp con tự định nghĩa (đa hình). */
    public abstract String printInfo();

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return id.equals(((Entity) other).id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }
}