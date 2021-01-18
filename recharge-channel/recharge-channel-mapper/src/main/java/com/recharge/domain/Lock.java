package com.recharge.domain;

public class Lock {

    public static String LOCK_GETORDER = "LOCK_GETORDER";

    private String lockId;

    private String user;

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
