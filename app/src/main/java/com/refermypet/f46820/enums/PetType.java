package com.refermypet.f46820.enums;

public enum PetType {
    DOG,
    CAT,
    BIRD,
    GUINEA_PIG,
    OTHER;

    @Override
    public String toString() {
        String name = name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
