/*
 * Copyright 2013 Cloudera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.cdk.data.spi;

import com.cloudera.cdk.data.DatasetDescriptor;
import com.cloudera.cdk.data.MetadataProvider;
import com.cloudera.cdk.data.NoSuchDatasetException;

/**
 * A common DatasetRepository base class to simplify implementation.
 *
 * Implementers can use this class to maintain compatibility with old API calls,
 * without needing to implement deprecated methods. It also includes backwards-
 * compatible implementations of current API methods so that implementers don't
 * need to implement deprecated methods.
 */
public abstract class AbstractMetadataProvider implements MetadataProvider {

  // for detecting call loops; remove in 0.8.x
  private boolean inSave = false;

  @Override
  public DatasetDescriptor create(String name, DatasetDescriptor descriptor) {
    if (inSave) {
      throw new UnsupportedOperationException(
          "You must implement MetadataProvider#create");
    }
    save(name, descriptor);
    return descriptor;
  }

  @Override
  public DatasetDescriptor update(String name, DatasetDescriptor descriptor) {
    if (inSave) {
      throw new UnsupportedOperationException(
          "You must implement MetadataProvider#create");
    }
    save(name, descriptor);
    return descriptor;
  }

  @Deprecated
  @Override
  public void save(String name, DatasetDescriptor descriptor) {
    try {
      this.inSave = true;
      boolean exists;
      try {
        DatasetDescriptor oldDescriptor = load(name);
        exists = (oldDescriptor != null);
      } catch (NoSuchDatasetException ex) {
        exists = false;
      }

      if (exists) {
        update(name, descriptor);
      } else {
        create(name, descriptor);
      }
    } finally {
      this.inSave = false;
    }
  }

}