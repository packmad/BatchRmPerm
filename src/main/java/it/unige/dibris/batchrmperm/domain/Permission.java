package it.unige.dibris.batchrmperm.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Permission {
    @Id
    private String permissionName;

    Permission() {}

    public Permission(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return permissionName.equals(that.permissionName);
    }

    @Override
    public int hashCode() {
        return permissionName.hashCode();
    }
}
