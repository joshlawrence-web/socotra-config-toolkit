package com.socotra.coremodel;

/** Customer defined structure with data extension fields */
@Deprecated // Use CustomerObject and SensitiveDataHolder separately instead
public interface CustomerObjectWithData<T> extends SensitiveDataHolder<T>, CustomerObject {}
