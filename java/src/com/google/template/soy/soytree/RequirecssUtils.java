/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.template.soy.soytree;

import com.google.common.collect.ImmutableList;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.base.internal.BaseUtils;

import javax.annotation.Nullable;


/**
 * Utilities for processing the 'requirecss' attribute (for a Soy file or for a template).
 *
 * <p>Important: Do not use outside of Soy code (treat as superpackage-private).
 *
 * @author Kai Huang
 */
public class RequirecssUtils {


  // Disallow instantiation.
  private RequirecssUtils() {}


  /**
   * Parses a 'requirecss' attribute value (for a Soy file or a template).
   *
   * @param requirecssAttr The 'requirecss' attribute value to parse.
   * @return A list of required CSS namespaces parsed from the given attribute value.
   */
  public static ImmutableList<String> parseRequirecssAttr(@Nullable String requirecssAttr) {

    if (requirecssAttr == null) {
      return ImmutableList.of();
    }

    String[] namespaces = requirecssAttr.trim().split("\\s*,\\s*");
    for (String namespace : namespaces) {
      if (! BaseUtils.isDottedIdentifier(namespace)) {
        throw SoySyntaxException.createWithoutMetaInfo(
            "Invalid required CSS namespace name \"" + namespace + "\".");
      }
    }
    return ImmutableList.copyOf(namespaces);
  }

}
