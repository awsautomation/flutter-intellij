/*
 * Copyright (c) 2015, the Dart project authors.
 *
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dartlang.vm.service.element;

// This file is generated by the script: pkg/vm_service/tool/generate.dart in dart-lang/sdk.

import com.google.gson.JsonObject;

/**
 * See RetainingPath.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RetainingObject extends Element {

  public RetainingObject(JsonObject json) {
    super(json);
  }

  /**
   * If `value` is a non-List, non-Map object, `parentField` is the name of the field containing
   * the previous object on the retaining path.
   *
   * @return one of <code>String</code> or <code>int</code>
   *
   * Can return <code>null</code>.
   */
  public Object getParentField() {
    final JsonObject elem = (JsonObject)json.get("parentField");
    if (elem == null) return null;

    // TODO(messick): Verify this is correct. I had to modify the generated code.
    if (elem.get("type").getAsString().equals("String")) return elem.get("value").getAsString();
    if (elem.get("type").getAsString().equals("int")) {
      try {
        return Integer.parseInt(elem.get("value").getAsString());
      } catch (NumberFormatException ex) {
        // ignored
      }
    }
    return null;
  }

  /**
   * If `value` is a List, `parentListIndex` is the index where the previous object on the
   * retaining path is located (deprecated).
   *
   * Note: this property is deprecated and will be replaced by `parentField`.
   *
   * Can return <code>null</code>.
   */
  public int getParentListIndex() {
    return getAsInt("parentListIndex");
  }

  /**
   * If `value` is a Map, `parentMapKey` is the key mapping to the previous object on the retaining
   * path.
   *
   * Can return <code>null</code>.
   */
  public ObjRef getParentMapKey() {
    JsonObject obj = (JsonObject) json.get("parentMapKey");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ObjRef(obj);
  }

  /**
   * An object that is part of a retaining path.
   */
  public ObjRef getValue() {
    return new ObjRef((JsonObject) json.get("value"));
  }
}
