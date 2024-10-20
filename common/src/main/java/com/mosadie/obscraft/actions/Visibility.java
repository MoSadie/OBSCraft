package com.mosadie.obscraft.actions;

public enum Visibility {
    SHOW,
    HIDE;

    public boolean isVisible() {
        return this == SHOW;
    }
}
