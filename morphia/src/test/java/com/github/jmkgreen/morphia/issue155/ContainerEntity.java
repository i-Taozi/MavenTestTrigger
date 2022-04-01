package com.github.jmkgreen.morphia.issue155;

import com.github.jmkgreen.morphia.testutil.TestEntity;

class ContainerEntity extends TestEntity {
    private static final long serialVersionUID = 1L;
    
    Bar foo = EnumBehindAnInterface.A;
}